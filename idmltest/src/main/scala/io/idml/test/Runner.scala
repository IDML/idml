package io.idml.test
import java.io.File
import java.nio.file.{Path, Paths, StandardOpenOption}

import cats._
import cats.data.EitherT
import cats.implicits._
import cats.effect._
import io.circe.parser.{parse => parseJson}
import io.circe.yaml.parser.{parse => parseYaml}
import io.circe.{Decoder, Json}
import io.circe.generic.auto._
import io.circe.syntax._
import io.idml.{Ptolemy, PtolemyConf, PtolemyJson, StaticFunctionResolverService}
import fs2._
import gnieh.diffson.circe._

import scala.util.Try
import scala.collection.JavaConverters._

class TestUtils[F[_]: Sync] {
  def readAll(p: Path): F[String]              = fs2.io.file.readAll[F](p, 2048).through(fs2.text.utf8Decode[F]).compile.foldMonoid
  def parseJ(s: String): F[Json]               = Sync[F].fromEither(parseJson(s))
  def parseY(s: String): F[Json]               = Sync[F].fromEither(parseYaml(s))
  def as[T: Decoder](j: Json): F[T]            = Sync[F].fromEither(j.as[T])
  def refToPath(parent: Path, r: Ref): F[Path] = Sync[F].fromTry(Try { Paths.get(r.`$ref`) })
  def writeAll(p: Path)(s: Stream[F, String]): F[Unit] =
    s.through(fs2.text.utf8Encode[F]).to(fs2.io.file.writeAll(p, List(StandardOpenOption.TRUNCATE_EXISTING))).compile.drain
  def print(a: Any): F[Unit]         = Sync[F].delay { println(a) }
  def red[T <: Any](t: T): F[Unit]   = Sync[F].delay { println(fansi.Color.Red(t.toString)) }
  def green[T <: Any](t: T): F[Unit] = Sync[F].delay { println(fansi.Color.Green(t.toString)) }
  def blue[T <: Any](t: T): F[Unit]  = Sync[F].delay { println(fansi.Color.Cyan(t.toString)) }
}

object Runner extends TestUtils[IO] with CirceEitherEncoders {

  case class State(folder: Path)

  def load(test: Path): IO[Tests] = readAll(test).flatMap(parseJ).flatMap(as[Tests])
  def resolve(path: Path, tests: Tests): IO[List[ResolvedTest]] =
    tests.tests.traverse(
      _.resolve(
        refToPath(path, _).flatMap(readAll),
        refToPath(path, _).flatMap(readAll).flatMap(parseJ)
      ))
  def updateResolve(path: Path, tests: Tests): IO[List[UpdateableResolvedTest]] =
    tests.tests.traverse(
      _.updateResolve(
        refToPath(path, _).flatMap(readAll),
        refToPath(path, _).flatMap(readAll).flatMap(parseJ)
      ))

  def ptolemy: IO[Ptolemy] = IO {
    new Ptolemy(
      new PtolemyConf(),
      new StaticFunctionResolverService((new DeterministicTime() :: StaticFunctionResolverService.defaults.asScala.toList).asJava)
    )
  }

  def run(code: String, input: Json): IO[Json] =
    for {
      p <- ptolemy
      m <- IO { p.fromString(code) }
      r <- IO { PtolemyJson.compact(m.run(PtolemyJson.parse(input.toString()))) }
      c <- parseJ(r)
    } yield c

  def runTest(failedOnly: Boolean)(path: Path) =
    for {
      t <- load(path)
      result <- EitherT(resolve(path, t).attempt)
                 .semiflatMap { resolved =>
                   resolved.traverse(r => run(r.code, r.input).tupleLeft(r))
                 }
                 .map {
                   _.map {
                     case (resolved, output) =>
                       Either.cond(resolved.output === output,
                                   resolved.name,
                                   (resolved.name, JsonDiff.simpleDiff(output, resolved.output, true)))
                   }
                 }
                 .value
      _ <- result.bitraverse(
            { e =>
              red(s"$path errored when loading") *>
                red(e)
            }, {
              _.traverse {
                _.bitraverse({
                  case (name, diff) =>
                    red(s"$name output differs") *>
                      red(diff)
                }, { name =>
                  IO.pure(failedOnly).ifM(IO.unit, green(s"${name} passed"))
                })
              }
            }
          )
    } yield ()

//  _         <- blue(s"${test.name} updated")
  def updateTest(failedOnly: Boolean)(path: Path) =
    for {
      test      <- load(path)
      updatable <- updateResolve(path, test)
      result    <- updatable.traverse(u => run(u.code, u.input).tupleLeft(u))
      updated <- result.traverse {
                  case (u, result) =>
                    u.output
                      .bitraverse(
                        // we've got a referred output
                        { r =>
                          for {
                            p           <- refToPath(path, r)
                            oldcontents <- readAll(p)
                            contents    = result.spaces2
                            _ <- IO
                                  .pure(contents === oldcontents)
                                  .ifM(
                                    IO.pure(failedOnly).ifM(IO.unit, green(s"${u.name} unchanged")),
                                    blue(s"${u.name} updated") *> writeAll(p)(Stream.emit(contents))
                                  )
                          } yield u.original
                        },
                        // we've got an inline output
                        { expected =>
                          IO.pure(expected === result)
                            .ifM(
                              IO.pure(failedOnly).ifM(IO.unit, green(s"${u.name} unchanged")).as(Left(u.original)),
                              blue(s"${u.name} updated inline").as(Right(u.original.copy(output = Right(result))))
                            )
                        }
                      )
                      .map(_.leftMap(_.asLeft[Test]).merge)
                }
      // if we had any right entries it means we've got to update this file
      _ <- updated
            .exists(_.isRight)
            .pure[IO]
            .ifM(
              blue(s"flushing update to $path") *> writeAll(path)(Stream.emit(Tests(updated.map(_.merge)).asJson.spaces2)),
              failedOnly.pure[IO].ifM(IO.unit, green(s"$path unchanged, not flushing file"))
            )
    } yield ()

}
