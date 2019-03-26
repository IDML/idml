package io.idml.test
import java.io.File
import java.nio.file.{Path, Paths}

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
  def readAll(p: Path): F[String]                      = fs2.io.file.readAll[F](p, 2048).through(fs2.text.utf8Decode[F]).compile.foldMonoid
  def parseJ(s: String): F[Json]                       = Sync[F].fromEither(parseJson(s))
  def parseY(s: String): F[Json]                       = Sync[F].fromEither(parseYaml(s))
  def as[T: Decoder](j: Json): F[T]                    = Sync[F].fromEither(j.as[T])
  def refToPath(parent: Path, r: Ref): F[Path]         = Sync[F].fromTry(Try { Paths.get(r.`$ref`) })
  def writeAll(p: Path)(s: Stream[F, String]): F[Unit] = s.through(fs2.text.utf8Encode[F]).to(fs2.io.file.writeAll(p)).compile.drain
  def print(a: Any): F[Unit]                           = Sync[F].delay { println(a) }
  def red[T <: Any](t: T): F[Unit]                     = Sync[F].delay { println(fansi.Color.Red(t.toString)) }
  def green[T <: Any](t: T): F[Unit]                   = Sync[F].delay { println(fansi.Color.Green(t.toString)) }
  def blue[T <: Any](t: T): F[Unit]                    = Sync[F].delay { println(fansi.Color.Cyan(t.toString)) }
}

object Runner extends TestUtils[IO] with CirceEitherEncoders {

  case class State(folder: Path)

  def load(test: Path) = readAll(test).flatMap(parseJ).flatMap(as[Test])
  def resolve(path: Path, test: Test): IO[ResolvedTest] = test.resolve(
    refToPath(path, _).flatMap(readAll),
    refToPath(path, _).flatMap(readAll).flatMap(parseJ)
  )
  def updateResolve(path: Path, test: Test): IO[UpdateableResolvedTest] = test.updateResolve(
    refToPath(path, _).flatMap(readAll),
    refToPath(path, _).flatMap(readAll).flatMap(parseJ)
  )

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

  def runTest(path: Path) =
    for {
      t <- load(path)
      result <- EitherT(resolve(path, t).attempt)
                 .semiflatMap { resolved =>
                   run(resolved.code, resolved.input).tupleLeft(resolved)
                 }
                 .map {
                   case (resolved, output) =>
                     Either.cond(resolved.output === output, Unit, JsonDiff.simpleDiff(output, resolved.output, true))
                 }
                 .value
      _ <- result.bitraverse({ e =>
            red(s"${t.name} errored") *>
              red(e)
          }, {
            _.bitraverse({ diff =>
              red(s"${t.name} output differs") *>
                red(diff)
            }, { _ =>
              green(s"${t.name} passed")
            })
          })
    } yield Unit

  def updateTest(path: Path) =
    for {
      test      <- load(path)
      updatable <- updateResolve(path, test)
      result    <- run(updatable.code, updatable.input)
      _         <- blue(s"${test.name} updated")
      updated <- updatable.output.bitraverse(
                  { r =>
                    for {
                      p           <- refToPath(path, r)
                      oldcontents <- readAll(p)
                      contents    = result.spaces2
                      _           <- writeAll(p)(Stream.emit(contents))
                    } yield (oldcontents, contents)
                  }, { _ =>
                    for {
                      oldcontents <- readAll(path)
                      contents    = test.copy(output = Right(result)).asJson.spaces2
                      _           <- writeAll(path)(Stream.emit(contents))
                    } yield (oldcontents, contents)
                  }
                )
    } yield Unit

}
