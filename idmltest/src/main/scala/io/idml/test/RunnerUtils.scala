package io.idml.test

import java.net.URL

import cats._
import cats.data.{EitherT, NonEmptyList}
import cats.implicits._
import cats.effect._
import io.circe.Json
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
import Test._

import scala.collection.JavaConverters._

class RunnerUtils(dynamic: Boolean, plugins: Option[NonEmptyList[URL]]) extends TestUtils[IO] {

  def ptolemy(time: Option[Long]): IO[Ptolemy] = IO {
    val baseFunctionResolver =
      new StaticFunctionResolverService(
        (new DeterministicTime(time.getOrElse(0L)) :: StaticFunctionResolverService.defaults.asScala.toList).asJava)
    val frs = plugins.fold[FunctionResolverService](
      baseFunctionResolver
    )(
      urls => FunctionResolverService.orElse(baseFunctionResolver, new PluginFunctionResolverService(urls.toList.toArray)),
    )
    new Ptolemy(
      new PtolemyConf(),
      if (dynamic) frs.orElse(new FunctionResolverService())
      else frs
    )
  }

  def run[T, O](f: (PtolemyMapping, T) => IO[O])(time: Option[Long], code: String, input: T): IO[O] =
    for {
      p <- ptolemy(time.map(_ * 1000))
      m <- IO { p.fromString(code) }
      r <- f(m, input)
    } yield r

  def runSingle: (Option[Long], String, Json) => IO[Json] =
    run({ (m: PtolemyMapping, j: Json) =>
      IO {
        PtolemyJson.compact(m.run(PtolemyJson.parse(j.toString)))
      }.flatMap(parseJ)
    })

  def runMulti: (Option[Long], String, List[Json]) => IO[List[Json]] =
    run({ (m: PtolemyMapping, js: List[Json]) =>
      js.traverse { j =>
        IO { PtolemyJson.compact(m.run(PtolemyJson.parse(j.toString))) }.flatMap(parseJ)
      }
    })

  val spaces2butDropNulls = spaces2.copy(dropNullValues = true)

  case class DifferentOutput(name: String, diff: String)

  trait PtolemyUtils[T] {
    def run: (Option[Long], String, T) => IO[T]
    def toString(t: T): String
    def validate(t: ResolvedTest[T]): Either[Throwable, ResolvedTest[T]]
    def inspectOutput(resolved: ResolvedTest[T], output: T, diff: (Json, Json) => String): Either[DifferentOutput, List[String]]
  }
  object PtolemyUtils {
    def apply[T: PtolemyUtils]: PtolemyUtils[T] = implicitly
  }
  implicit val singlePtolemyRun = new PtolemyUtils[Json] {
    override def run                             = runSingle
    override def toString(t: Json)               = spaces2butDropNulls.pretty(t)
    override def validate(t: ResolvedTest[Json]) = Right(t)
    override def inspectOutput(resolved: ResolvedTest[Json],
                               output: Json,
                               diff: (Json, Json) => String): Either[DifferentOutput, List[String]] =
      Either.cond(
        resolved.output === output,
        List(resolved.name),
        DifferentOutput(resolved.name, diff(output, resolved.output))
      )

  }
  implicit val multiPtolemyRun = new PtolemyUtils[List[Json]] {
    override def run                     = runMulti
    override def toString(t: List[Json]) = spaces2butDropNulls.pretty(Json.arr(t: _*))
    override def validate(t: ResolvedTest[List[Json]]) = Either.cond(
      t.input.size == t.output.size,
      t,
      new Throwable(s"${t.name} must have the same number of inputs and outputs")
    )
    override def inspectOutput(resolved: ResolvedTest[List[Json]],
                               output: List[Json],
                               diff: (Json, Json) => String): Either[DifferentOutput, List[String]] = {
      resolved.output.zip(output).traverse {
        case (expected, actual) =>
          Either.cond(
            expected.asJson === actual,
            resolved.name,
            DifferentOutput(resolved.name, diff(actual, expected))
          )
      }
    }
  }
}
