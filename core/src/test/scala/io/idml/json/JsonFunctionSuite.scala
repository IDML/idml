package io.idml.json

import io.idml.{Ptolemy, PtolemyJson, PtolemyValue}
import io.idml.datanodes._
import io.idml.functions.json.JsonFunctions
import org.scalatest.{MustMatchers, WordSpec}

class JsonFunctionSuite(name: String, jf: JsonFunctions) extends WordSpec with MustMatchers {
   name should {
    "serialize objects" in {
      jf.obj.serialize(PObject("v" -> PInt(123))) must equal(PString("""{"v":123}"""))
    }
    "parse objects" in {
      jf.obj.parseJson(PString("""{"v":123}""")) must equal(PObject("v" -> PInt(123)))
    }
    "be loaded automatically" in {
      val p       = new Ptolemy()
      val program = p.fromString("""
          |uuid3 = root.uuid3()
          |uuid5 = root.uuid5()
          |object = js.parseJson()
          |alsoObject = {"v":123}.serialize()
          |number = root.random()
          |anotherNumber = root.random(1, 100)
          |""".stripMargin)
      val result  = program.run(PObject("hello" -> PtolemyValue("world"), "js" -> PString("{\"hello\": \"world\"}")))
      result must equal(
        PObject(
          "alsoObject"    -> PString("""{"v":123}"""),
          "anotherNumber" -> PInt(80),
          "number"        -> PInt(-8141464344866794837L),
          "object"        -> PObject("hello" -> PString("world")),
          "uuid3"         -> PString("7471b180-e84d-33a4-b395-33354a28479c"),
          "uuid5"         -> PString("a55bb9b5-e633-5876-b34c-39c5ece44ba9")
        ))
    }
  }
}
