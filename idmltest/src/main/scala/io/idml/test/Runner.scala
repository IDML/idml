package io.idml.test
import java.net.URL
import java.nio.file.{Path, StandardOpenOption}

import cats._
import cats.data.{EitherT, NonEmptyList}
import cats.implicits._
import cats.effect._
import com.google.re2j.Pattern
import io.circe.parser.{parse => parseJson}
import io.circe.yaml.parser.{parse => parseYaml}
import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.auto._
import io.circe.syntax._
import fs2._
import gnieh.diffson.circe._
import diffable.TestDiff
import Test._

import scala.concurrent.ExecutionContext
import scala.util.Try

class TestUtils[F[_]: Sync] {
  def readAll(ec: ExecutionContext)(p: Path)(implicit cs: ContextShift[F]): F[String] =
    fs2.io.file.readAll[F](p, ec, 2048).through(fs2.text.utf8Decode[F]).compile.foldMonoid
  def parseJ(s: String): F[Json]    = Sync[F].fromEither(parseJson(s))
  def parseY(s: String): F[Json]    = Sync[F].fromEither(parseYaml(s))
  def as[T: Decoder](j: Json): F[T] = Sync[F].fromEither(j.as[T])
  def refToPath(parent: Path, r: Ref): F[Path] = Sync[F].fromTry(
    Try { parent.toAbsolutePath.getParent.resolve(r.`$ref`) }
  )
  def writeAll(ec: ExecutionContext)(p: Path)(s: Stream[F, String])(implicit cs: ContextShift[F]): F[Unit] =
    s.through(fs2.text.utf8Encode[F])
      .to(fs2.io.file.writeAll(p, ec, List(StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)))
      .compile
      .drain
  def print(a: Any): F[Unit]         = Sync[F].delay { println(a) }
  def red[T <: Any](t: T): F[Unit]   = print(fansi.Color.Red(t.toString))
  def green[T <: Any](t: T): F[Unit] = print(fansi.Color.Green(t.toString))
  def blue[T <: Any](t: T): F[Unit]  = print(fansi.Color.Cyan(t.toString))
}

class Runner(dynamic: Boolean, plugins: Option[NonEmptyList[URL]], jdiff: Boolean, blockingEc: ExecutionContext)(
    implicit cs: ContextShift[IO])
    extends RunnerUtils(dynamic, plugins)
    with CirceEitherEncoders {

  def load(test: Path): IO[Either[Tests[List[Json]], Tests[Json]]] =
    readAll(blockingEc)(test).flatMap(parseJ).flatMap(as[Either[Tests[List[Json]], Tests[Json]]])
  def resolve[T: Decoder](path: Path, tests: Tests[T]): IO[List[ResolvedTest[T]]] =
    tests.tests.traverse(
      _.resolve(
        refToPath(path, _).flatMap(readAll(blockingEc)),
        parseJ
      ))
  def updateResolve[T: Decoder](path: Path, tests: Tests[T]): IO[List[UpdatableTest[T]]] =
    tests.tests.traverse(
      _.updateResolve(
        refToPath(path, _).flatMap(readAll(blockingEc)),
        parseJ
      ))

  def patternToFilter(filter: Option[Pattern]): String => Boolean = (s: String) => filter.map(_.matches(s)).getOrElse(true)

  def runTest(failedOnly: Boolean, filter: Option[Pattern] = None)(cs: ContextShift[IO])(path: Path): IO[List[TestState]] = {
    implicit val ics: ContextShift[IO] = cs
    for {
      t <- load(path)
      r <- t.bitraverse(runTests[List[Json]](failedOnly, filter)(path), runTests[Json](failedOnly, filter)(path))
    } yield r.merge
  }

  def runTests[T: Encoder: Decoder: Eq: PtolemyUtils](failedOnly: Boolean, filter: Option[Pattern] = None)(path: Path)(t: Tests[T])(
      implicit cs: ContextShift[IO]): IO[List[TestState]] =
    for {
      result <- EitherT(resolve(path, t).attempt)
                 .flatMap { resolved =>
                   resolved.traverse { t =>
                     EitherT.fromEither[IO](PtolemyUtils[T].validate(t))
                   }
                 }
                 .semiflatMap { resolved =>
                   resolved
                     .filter(patternToFilter(filter).compose(_.name))
                     .parTraverse(r => PtolemyUtils[T].run(implicitly)(r.time, r.code, r.input).tupleLeft(r))
                 }
                 .map {
                   _.map {
                     case (resolved, output) =>
                       PtolemyUtils[T].inspectOutput(resolved, output, if (jdiff) { (j1: Json, j2: Json) =>
                         JsonDiff.simpleDiff(j1, j2, true).toString()
                       } else { (j1: Json, j2: Json) =>
                         TestDiff.generateDiff(j1, j2)
                       })
                   }
                 }
                 .value
      outputs <- result.bitraverse(
                  { e =>
                    red(s"${path.getFileName} errored when loading") *>
                      red(e).as(TestState.error)
                  }, {
                    _.traverse {
                      _.traverse(
                        _.bitraverse(
                          {
                            case DifferentOutput(name, diff) =>
                              red(s"$name output differs") *>
                                print(diff).as(TestState.failed)
                          }, { name =>
                            IO.pure(failedOnly).ifM(IO.unit, green(s"$name passed")).as(TestState.success)
                          }
                        )
                      )
                    }
                  }
                )
    } yield outputs.leftMap(List(_)).map(_.flatten.map(_.merge)).merge

  def updateTest(failedOnly: Boolean, filter: Option[Pattern] = None)(cs: ContextShift[IO])(path: Path): IO[List[TestState]] = {
    implicit val ics: ContextShift[IO] = cs
    for {
      test <- load(path)
      r    <- test.bitraverse(updateTests[List[Json]](failedOnly, filter)(path), updateTests[Json](failedOnly, filter)(path))
    } yield r.merge
  }

  def updateTests[T: Encoder: Decoder: Eq](failedOnly: Boolean, filter: Option[Pattern] = None)(path: Path)(
      test: Tests[T])(implicit runner: PtolemyUtils[T], cs: ContextShift[IO]): IO[List[TestState]] =
    for {
      updatable <- updateResolve(path, test).attempt
      result <- updatable.bitraverse(
                 e =>
                   red(s"$path errored when loading") *>
                     red(e).as(TestState.error),
                 _.parTraverse(u => runner.run(implicitly)(u.time, u.code, u.input).tupleLeft(u))
               )
      updated <- result.traverse(_.filter(patternToFilter(filter).compose(_._1.name)).traverse {
                  case (u, result) =>
                    u.output
                      .bitraverse(
                        // we've got a referred output
                        { r =>
                          for {
                            p           <- refToPath(path, r)
                            oldcontents <- readAll(blockingEc)(p).attempt.map(_.leftMap(_ => "").merge)
                            contents    = runner.toString(result)
                            status <- IO
                                       .pure(contents === oldcontents)
                                       .ifM(
                                         IO.pure(failedOnly).ifM(IO.unit, green(s"${u.name} unchanged")).as(TestState.success),
                                         blue(s"${u.name} updated") *> writeAll(blockingEc)(p)(Stream.emit(contents)).as(TestState.updated)
                                       )
                          } yield (status, u.original.get)
                        },
                        // we've got an inline output
                        { expected =>
                          IO.pure(expected === result)
                            .ifM(
                              IO.pure(failedOnly)
                                .ifM(IO.unit, green(s"${u.name} unchanged"))
                                .as((TestState.success, u.original.get)
                                  .asLeft[(TestState, ParsedTest[T])]),
                              blue(s"${u.name} updated inline").as((TestState.updated, u.original.get.copy(output = Right(result)))
                                .asInstanceOf[(TestState, ParsedTest[T])]
                                .asRight[(TestState, ParsedTest[T])])
                            )
                        }
                      )
                      .map(_.leftMap(_.asLeft[(TestState, ParsedTest[T])]).merge)
                })
      // if we had any right entries it means we've got to update this file
      exit <- updated.traverse { u =>
               u.exists(_.isRight)
                 .pure[IO]
                 .ifM(
                   blue(s"flushing update to ${path.getFileName}")
                     *> writeAll(blockingEc)(path)(Stream.emit(spaces2butDropNulls.pretty(Tests(u.map(_.merge._2)).asJson)))
                       .as(TestState.updated),
                   failedOnly.pure[IO].ifM(IO.unit, green(s"${path.getFileName} unchanged, not flushing file")).as(TestState.success)
                 )
             }
    } yield List(exit.merge) ++ updated.toOption.toList.flatten.map(_.bimap(_._1, _._1).merge)

  def pluralize(s: String)(count: Int): String = if (count == 1) s else s"${s}s"

  def report(results: List[TestState]): IO[Unit] = {
    print("---") *>
      print("Test Summary:") *>
      results
        .groupBy(identity)
        .mapValues(_.size)
        .toList
        .map {
          case (s, count) =>
            (count > 0)
              .pure[IO]
              .ifM(
                s match {
                  case TestState.Error   => red(s"$count ${pluralize("test")(count)} errored")
                  case TestState.Failed  => red(s"$count ${pluralize("test")(count)} failed")
                  case TestState.Updated => blue(s"$count ${pluralize("test")(count)} updated")
                  case TestState.Success => green(s"$count ${pluralize("test")(count)} succeeded")
                },
                IO.unit
              )
        }
        .combineAll
  }

}
