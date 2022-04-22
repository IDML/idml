package io.idml.utils

import configuration.Pipeline
import Pipeline._
import ConfigurationMapping._
import atto._
import Atto._
import cats.data.NonEmptyList
import cats.implicits._
import io.idml.IdmlBuilder
import io.idml.datanodes.{IBool, IInt, IObject, IString}
import org.scalatest.matchers.must
import org.scalatest.wordspec.AnyWordSpec

class ConfigurationDslSpec extends AnyWordSpec with must.Matchers {

  val parse: String => Either[String, ConfigurationMapping] =
    Pipeline.Parser.apply[Either[String, *]](_)
  "The DSL parser" should {
    "parse single mappings" in {
      parse("abc") must equal(Right(SingleMapping("abc")))
    }
    "parse chained mappings" in {
      parse("a|b") must equal(Right(Chained(SingleMapping("a"), SingleMapping("b"))))
    }
    "parse merged mappings" in {
      parse("a+b") must equal(Right(Merged(SingleMapping("a"), SingleMapping("b"))))
    }
    "parse more complex mappings" in {
      parse("a+b|c|d+e") must equal(
        Right(
          Chained(
            Merged(SingleMapping("a"), SingleMapping("b")),
            Chained(
              SingleMapping("c"),
              Merged(SingleMapping("d"), SingleMapping("e"))
            )
          )
        )
      )
    }
  }
  "The DSL" should {
    val idml = IdmlBuilder.withStaticFunctions().build()
    "be able to run two mappings" in {
      Pipeline
        .run { s =>
          Either.fromOption(
            Map(
              "a" -> idml.compile("map = \"a\"\na = true"),
              "b" -> idml.compile("map = \"b\"\nb = true")
            ).get(s),
            "Could not find mapping"
          )
        }
        .apply("a+b")
        .map { m =>
          m.run(IObject())
        } must equal(
        Right(
          IObject(
            "map" -> IString("b"),
            "a"   -> IBool(true),
            "b"   -> IBool(true)
          )
        )
      )
    }
    "should be able to chain two mappings" in {
      Pipeline
        .run { s =>
          Either.fromOption(
            Map(
              "a" -> idml.compile("aout = input + 1"),
              "b" -> idml.compile("bout = aout + 1")
            ).get(s),
            "Could not find mapping"
          )
        }
        .apply("a|b")
        .map { m =>
          m.run(IObject("input" -> IInt(0)))
        } must equal(
        Right(
          IObject(
            "bout" -> IInt(2),
            "aout" -> IInt(1)
          )
        )
      )
    }
  }

}
