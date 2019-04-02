package io.idml.test
import java.io.File
import java.net.URL
import java.nio.file.{Path, Paths, StandardOpenOption}

import cats._
import cats.data.{EitherT, NonEmptyList}
import cats.implicits._
import cats.effect._
import com.google.re2j.Pattern
import io.circe.parser.{parse => parseJson}
import io.circe.yaml.parser.{parse => parseYaml}
import io.circe.{Decoder, Encoder, Json, JsonObject}
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.Printer.spaces2
import io.idml.{
  FunctionResolverService,
  PluginFunctionResolverService,
  Ptolemy,
  PtolemyConf,
  PtolemyJson,
  PtolemyMapping,
  StaticFunctionResolverService
}
import fs2._
import gnieh.diffson.circe._
import diffable.TestDiff
import Test._

import scala.util.Try
import scala.collection.JavaConverters._

class TestUtils[F[_]: Sync] {
  def readAll(p: Path): F[String]   = fs2.io.file.readAll[F](p, 2048).through(fs2.text.utf8Decode[F]).compile.foldMonoid
  def parseJ(s: String): F[Json]    = Sync[F].fromEither(parseJson(s))
  def parseY(s: String): F[Json]    = Sync[F].fromEither(parseYaml(s))
  def as[T: Decoder](j: Json): F[T] = Sync[F].fromEither(j.as[T])
  def refToPath(parent: Path, r: Ref): F[Path] = Sync[F].fromTry(
    Try { parent.toAbsolutePath.getParent.resolve(r.`$ref`) }
  )
  def writeAll(p: Path)(s: Stream[F, String]): F[Unit] =
    s.through(fs2.text.utf8Encode[F])
      .to(fs2.io.file.writeAll(p, List(StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)))
      .compile
      .drain
  def print(a: Any): F[Unit]         = Sync[F].delay { println(a) }
  def red[T <: Any](t: T): F[Unit]   = print(fansi.Color.Red(t.toString))
  def green[T <: Any](t: T): F[Unit] = print(fansi.Color.Green(t.toString))
  def blue[T <: Any](t: T): F[Unit]  = print(fansi.Color.Cyan(t.toString))
}

class Runner(dynamic: Boolean, plugins: Option[NonEmptyList[URL]], jdiff: Boolean) extends RunnerUtils(dynamic, plugins) with CirceEitherEncoders {

  def load(test: Path): IO[Either[Tests[Json], Tests[List[Json]]]] =
    readAll(test).flatMap(parseJ).flatMap(as[Either[Tests[Json], Tests[List[Json]]]])
  def resolve[T: Decoder](path: Path, tests: Tests[T]): IO[List[ResolvedTest[T]]] =
    tests.tests.traverse(
      _.resolve(
        refToPath(path, _).flatMap(readAll),
        parseJ
      ))
  def updateResolve[T: Decoder](path: Path, tests: Tests[T]): IO[List[UpdatableTest[T]]] =
    tests.tests.traverse(
      _.updateResolve(
        refToPath(path, _).flatMap(readAll),
        parseJ
      ))

  def patternToFilter(filter: Option[Pattern]): String => Boolean = (s: String) => filter.map(_.matches(s)).getOrElse(true)

  def runTest(failedOnly: Boolean, filter: Option[Pattern] = None)(path: Path): IO[List[TestState]] =
    for {
      t <- load(path)
      r <- t.bitraverse(runTestSingle(failedOnly, filter)(path), runTestMulti(failedOnly, filter)(path))
    } yield r.merge

  def runTestSingle(failedOnly: Boolean, filter: Option[Pattern] = None)(path: Path)(t: Tests[Json]): IO[List[TestState]] =
    for {
      result <- EitherT(resolve(path, t).attempt)
                 .semiflatMap { resolved =>
                   resolved.filter(patternToFilter(filter).compose(_.name)).traverse(r => runSingle(r.time, r.code, r.input).tupleLeft(r))
                 }
                 .map {
                   _.map {
                     case (resolved, output) =>
                       Either.cond(
                         resolved.output.asJson === output,
                         resolved.name,
                         (resolved.name,
                          if (jdiff)
                            JsonDiff.simpleDiff(output, resolved.output, true).toString()
                          else
                            TestDiff.generateDiff(output, resolved.output.asJson))
                       )
                   }
                 }
                 .value
      outputs <- result.bitraverse(
                  { e =>
                    red(s"$path errored when loading") *>
                      red(e).as(TestState.Error)
                  }, {
                    _.traverse {
                      _.bitraverse(
                        {
                          case (name, diff) =>
                            red(s"$name output differs") *>
                              print(diff).as(TestState.Failed)
                        }, { name =>
                          IO.pure(failedOnly).ifM(IO.unit, green(s"${name} passed")).as(TestState.Success)
                        }
                      )
                    }
                  }
                )
    } yield outputs.leftMap(List(_)).map(_.map(_.merge)).merge

  def runTestMulti(failedOnly: Boolean, filter: Option[Pattern] = None)(path: Path)(t: Tests[List[Json]]): IO[List[TestState]] =
    for {
      result <- EitherT(resolve(path, t).attempt)
                 .flatMap { resolved =>
                   resolved.traverse { t =>
                     EitherT.cond[IO](
                       t.input.size == t.output.size,
                       t,
                       new Throwable(s"${t.name} must have the same number of inputs and outputs")
                     )
                   }
                 }
                 .semiflatMap { resolved =>
                   resolved.filter(patternToFilter(filter).compose(_.name)).traverse(r => runMulti(r.time, r.code, r.input).tupleLeft(r))
                 }
                 .map {
                   _.map {
                     case (resolved, output) =>
                       resolved.output.zip(output).traverse {
                         case (expected, actual) =>
                           Either.cond(
                             expected.asJson === actual,
                             resolved.name,
                             (resolved.name,
                              if (jdiff)
                                JsonDiff.simpleDiff(actual, resolved.output, true).toString()
                              else
                                TestDiff.generateDiff(actual, resolved.output.asJson))
                           )
                       }
                   }
                 }
                 .value
      outputs <- result.bitraverse(
                  { e =>
                    red(s"$path errored when loading") *>
                      red(e).as(TestState.Error)
                  }, {
                    _.traverse {
                      _.bitraverse(
                        {
                          case (name, diff) =>
                            red(s"$name output differs") *>
                              print(diff).as(TestState.Failed)
                        }, { name =>
                          IO.pure(failedOnly).ifM(IO.unit, green(s"${name} passed")).as(TestState.Success)
                        }
                      )
                    }
                  }
                )
    } yield outputs.leftMap(List(_)).map(_.map(_.merge)).merge


  def updateTest(failedOnly: Boolean, filter: Option[Pattern] = None)(path: Path): IO[List[TestState]] =
    for {
      test <- load(path)
      r    <- test.bitraverse(updateTests[Json](failedOnly, filter)(path), updateTests[List[Json]](failedOnly, filter)(path))
    } yield r.merge

  def updateTests[T: Encoder: Decoder: Eq](failedOnly: Boolean, filter: Option[Pattern] = None)(path: Path)(test: Tests[T])(
      implicit runner: PtolemyUtils[T]): IO[List[TestState]] =
    for {
      updatable <- updateResolve(path, test).attempt
      result <- updatable.bitraverse(
                 e =>
                   red(s"$path errored when loading") *>
                     red(e).as(TestState.Error),
                 _.traverse(u => runner.run(u.time, u.code, u.input).tupleLeft(u))
               )
      updated <- result.traverse(_.filter(patternToFilter(filter).compose(_._1.name)).traverse {
                  case (u, result) =>
                    u.output
                      .bitraverse(
                        // we've got a referred output
                        { r =>
                          for {
                            p           <- refToPath(path, r)
                            oldcontents <- readAll(p).attempt.map(_.leftMap(_ => "").merge)
                            contents    = runner.toString(result)
                            status <- IO
                                       .pure(contents === oldcontents)
                                       .ifM(
                                         IO.pure(failedOnly).ifM(IO.unit, green(s"${u.name} unchanged")).as(TestState.Success),
                                         blue(s"${u.name} updated") *> writeAll(p)(Stream.emit(contents)).as(TestState.Updated)
                                       )
                          } yield (status, u.original.get)
                        },
                        // we've got an inline output
                        { expected =>
                          IO.pure(expected === result)
                            .ifM(
                              IO.pure(failedOnly)
                                .ifM(IO.unit, green(s"${u.name} unchanged"))
                                .as((TestState.Success.asInstanceOf[TestState], u.original.get)
                                  .asLeft[(TestState, ParsedTest[T])]),
                              blue(s"${u.name} updated inline").as((TestState.Updated, u.original.get.copy(output = Right(result)))
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
                     *> writeAll(path)(Stream.emit(spaces2butDropNulls.pretty(Tests(u.map(_.merge._2)).asJson)))
                       .as(TestState.Updated),
                   failedOnly.pure[IO].ifM(IO.unit, green(s"${path.getFileName} unchanged, not flushing file")).as(TestState.Success)
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
