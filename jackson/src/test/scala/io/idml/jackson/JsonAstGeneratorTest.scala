package io.idml.jackson

import io.idml.Idml
import org.scalatest.{MustMatchers, WordSpec}
import JsonAstGenerator._
import org.json4s._

class JsonAstGeneratorTest extends WordSpec with MustMatchers {
  val p = new Idml()

  "JsonAstGenerator" when {
    "fed a simple single level schema with 1 item" should {
      "correctly represent the IDML as JSON" in {
        p.fromString("foo : int()").toJson must equal("""[{"operation":"reassignment","destination":"foo","type":"int"}]""")
        p.fromString("bar : string()").toJson must equal("""[{"operation":"reassignment","destination":"bar","type":"string"}]""")
        p.fromString("baz : bool()").toJson must equal("""[{"operation":"reassignment","destination":"baz","type":"bool"}]""")
        p.fromString("MixedCase : date()").toJson must equal("""[{"operation":"reassignment","destination":"MixedCase","type":"date"}]""")
        p.fromString("earl: url()").toJson must equal("""[{"operation":"reassignment","destination":"earl","type":"url"}]""")
      }
    }
    "fed a simple single level schema with an array" should {
      "correctly represent the IDML as JSON" in {
        p.fromString("numbers: array(int())").toJson must equal(
          """[{"operation":"reassignment","destination":"numbers","type":"array(int)"}]""")
      }
    }
    "fed an advanced multi level schema with an array" should {
      "correctly represent the IDML as JSON" in {
        p.fromString("numbers.value: array(int()).required()").toJson must equal(
          """[{"operation":"reassignment","destination":"numbers.value","type":"array(int)","required":true}]""")
      }
    }
    "fed a simple multi-level schema with 1 item" should {
      "correctly represent the IDML as JSON" in {
        p.fromString("foo.baz : int()").toJson must equal("""[{"operation":"reassignment","destination":"foo.baz","type":"int"}]""")
        p.fromString("bar.bing : string()").toJson must equal("""[{"operation":"reassignment","destination":"bar.bing","type":"string"}]""")
        p.fromString("baz.foo : bool()").toJson must equal("""[{"operation":"reassignment","destination":"baz.foo","type":"bool"}]""")
        p.fromString("MixedCase.imASnakeHonest.andIAmACamel : date()").toJson must equal(
          """[{"operation":"reassignment","destination":"MixedCase.imASnakeHonest.andIAmACamel","type":"date"}]""")
      }
    }
    "fed an advanced single level schema with 1 item" should {
      "correctly represent the IDML as JSON" in {
        p.fromString("foo : int().default(42)").toJson must equal(
          """[{"operation":"reassignment","destination":"foo","type":"int","default":42}]""")
      }
    }
    "fed an advanced multi level schema with 1 item" should {
      "correctly represent the IDML as JSON" in {
        p.fromString("foo.bar.baz: int().default(42)").toJson must equal(
          """[{"operation":"reassignment","destination":"foo.bar.baz","type":"int","default":42}]""")
        p.fromString("foo.bar.baz: string().required().size(100)").toJson must equal(
          """[{"operation":"reassignment","destination":"foo.bar.baz","type":"string","required":true,"size":100}]""")
      }
    }
    "fed an advanced multi-level schema with many items" should {
      "correctly represent the IDML as JSON" in {
        p.fromString("""
            |foo.bar.baz: int().default(42)
            |user.id: int().required()
            |i.like.turtles: bool().default(true).required()
          """.stripMargin).toJson must equal(
          """
            |[
            |{"operation":"reassignment","destination":"foo.bar.baz","type":"int","default":42},
            |{"operation":"reassignment","destination":"user.id","type":"int","required":true},
            |{"operation":"reassignment","destination":"i.like.turtles","type":"bool","default":true,"required":true}
            |]""".stripMargin.replace("\n", ""))
      }
    }
  }

}
