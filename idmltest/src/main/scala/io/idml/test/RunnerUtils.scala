package io.idml.test

import java.net.URL

import cats._
import cats.data.{EitherT, NonEmptyList}
import cats.implicits._
import cats.effect._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.Printer.spaces2
import io.idml.circe.IdmlCirce
import io.idml.circe.instances._
import io.idml._
import io.idml.utils.configuration.Pipeline
import fs2._
import Test._

import scala.collection.JavaConverters._
import scala.util.Try

class UnbalancedMultiTest(s: String) extends Throwable(s)

class RunnerUtils(dynamic: Boolean, plugins: Option[NonEmptyList[URL]]) extends TestUtils[IO] {

  def ptolemy(time: Option[Long]): IO[Idml] =
    IO {
      val baseFunctionResolver =
        new StaticFunctionResolverService(
          (new DeterministicTime(time.getOrElse(0L)) :: StaticFunctionResolverService
            .defaults(IdmlCirce)
            .asScala
            .toList).asJava
        )
      val frs                  = plugins.fold[FunctionResolverService](
        baseFunctionResolver
      )(urls =>
        FunctionResolverService.orElse(
          baseFunctionResolver,
          new PluginFunctionResolverService(urls.toList.toArray)))
      new IdmlBuilder(
        if (dynamic) frs.orElse(new FunctionResolverService())
        else frs
      ).build()
    }

  def run[T, O](f: (Mapping, T) => IO[O])(
      time: Option[Long],
      code: Either[String, PipelinedCode[String]],
      input: T): IO[O] =
    for {
      p <- ptolemy(time.map(_ * 1000))
      m <- code
             .bitraverse(
               c => IO { p.compile(c) },
               pc =>
                 IO.fromEither(
                   Pipeline
                     .run(name =>
                       Try { p.compile(pc.database(name)) }.toEither.leftMap(_.getMessage))
                     .apply(pc.pipeline)
                     .leftMap(s => new Throwable(s))
                 )
             )
             .map(_.merge)
      r <- f(m, input)
    } yield r

  def runSingle: (Option[Long], Either[String, PipelinedCode[String]], Json) => IO[Json] =
    run({ (m: Mapping, j: Json) =>
      IO {
        j.as[IdmlObject].map(m.run).map(_.asJson.foldWith(notNulls)).leftWiden[Throwable]
      }.rethrow
    })

  def runMulti(implicit timer: ContextShift[IO])
      : (Option[Long], Either[String, PipelinedCode[String]], List[Json]) => IO[List[Json]] =
    run({ (m: Mapping, js: List[Json]) =>
      js.parTraverse { j =>
        IO {
          j.as[IdmlObject].map(m.run).map(_.asJson.foldWith(notNulls)).leftWiden[Throwable]
        }.rethrow
      }
    })

  val spaces2butDropNulls = spaces2.copy(dropNullValues = true)

  val notNulls: Json.Folder[Json] = new Json.Folder[Json] {
    override def onObject(value: JsonObject): Json  = {
      Json.obj(
        value.toIterable
          .filterNot(_._2.isNull)
          .map(kv => (kv._1, kv._2.foldWith(notNulls)))
          .toVector: _*)
    }
    override def onNull: Json                       = Json.Null
    override def onBoolean(value: Boolean): Json    = Json.fromBoolean(value)
    override def onNumber(value: JsonNumber): Json  = Json.fromJsonNumber(value)
    override def onString(value: String): Json      = Json.fromString(value)
    override def onArray(value: Vector[Json]): Json = Json.arr(value.map(_.foldWith(notNulls)): _*)
  }

  case class DifferentOutput(name: String, diff: String)

  trait IdmlUtils[T] {
    def run(implicit
        timer: ContextShift[IO]): (Option[Long], Either[String, PipelinedCode[String]], T) => IO[T]
    def toString(t: T): String
    def validate(t: ResolvedTest[T]): Either[Throwable, ResolvedTest[T]]
    def inspectOutput(
        resolved: ResolvedTest[T],
        output: T,
        diff: (Json, Json) => String): List[Either[DifferentOutput, String]]
  }
  object IdmlUtils   {
    def apply[T: IdmlUtils]: IdmlUtils[T] = implicitly
  }
  implicit val singleIdmlRun = new IdmlUtils[Json] {
    override def run(implicit timer: ContextShift[IO]) = runSingle
    override def toString(t: Json)                     = spaces2butDropNulls.print(t)
    override def validate(t: ResolvedTest[Json])       = Right(t)
    override def inspectOutput(
        resolved: ResolvedTest[Json],
        output: Json,
        diff: (Json, Json) => String
    ): List[Either[DifferentOutput, String]] =
      List(
        Either.cond(
          resolved.output === output,
          resolved.name,
          DifferentOutput(resolved.name, diff(output, resolved.output))
        )
      )

  }
  implicit val multiIdmlRun = new IdmlUtils[List[Json]] {
    override def run(implicit timer: ContextShift[IO]) = runMulti
    override def toString(t: List[Json])               = spaces2butDropNulls.print(Json.arr(t: _*))
    override def validate(t: ResolvedTest[List[Json]]) =
      Either.cond(
        t.input.size == t.output.size,
        t,
        new UnbalancedMultiTest(s"${t.name} must have the same number of inputs and outputs")
      )
    override def inspectOutput(
        resolved: ResolvedTest[List[Json]],
        output: List[Json],
        diff: (Json, Json) => String
    ): List[Either[DifferentOutput, String]] = {
      (1 to resolved.output.length).zip(resolved.output.zip(output)).map {
        case (index, (expected, actual)) =>
          Either.cond(
            expected.asJson === actual,
            resolved.name + s" #$index",
            DifferentOutput(resolved.name + s" #$index", diff(actual, expected))
          )
      }
    }.toList
  }
}
