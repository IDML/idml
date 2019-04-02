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

  trait PtolemyUtils[T] {
    def run: (Option[Long], String, T) => IO[T]
    def toString(t: T): String
  }
  implicit val singlePtolemyRun = new PtolemyUtils[Json] {
    override def run               = runSingle
    override def toString(t: Json) = spaces2butDropNulls.pretty(t)
  }
  implicit val multiPtolemyRun = new PtolemyUtils[List[Json]] {
    override def run                     = runMulti
    override def toString(t: List[Json]) = spaces2butDropNulls.pretty(Json.arr(t: _*))
  }
}
