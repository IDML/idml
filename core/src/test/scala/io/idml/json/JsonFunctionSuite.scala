package io.idml.json

import io.idml.{Idml, IdmlJson, IdmlValue}
import io.idml.datanodes._
import io.idml.functions.json.JsonFunctions
import org.scalatest.{MustMatchers, WordSpec}

class JsonFunctionSuite(name: String, jf: JsonFunctions) extends WordSpec with MustMatchers {
  name should {
    "serialize objects" in {
      jf.obj.serialize(IObject("v" -> IInt(123))) must equal(IString("""{"v":123}"""))
    }
    "parse objects" in {
      jf.obj.parseJson(IString("""{"v":123}""")) must equal(IObject("v" -> IInt(123)))
    }
    "be loaded automatically" in {
      val idml    = Idml.createAuto(_.build())
      val program = idml.compile("""
          |uuid3 = root.uuid3()
          |uuid5 = root.uuid5()
          |object = js.parseJson()
          |alsoObject = {"v":123}.serialize()
          |number = root.random()
          |anotherNumber = root.random(1, 100)
          |""".stripMargin)
      val result  = program.run(IObject("hello" -> IdmlValue("world"), "js" -> IString("{\"hello\": \"world\"}")))
      result must equal(
        IObject(
          "alsoObject"    -> IString("""{"v":123}"""),
          "anotherNumber" -> IInt(80),
          "number"        -> IInt(-8141464344866794837L),
          "object"        -> IObject("hello" -> IString("world")),
          "uuid3"         -> IString("7471b180-e84d-33a4-b395-33354a28479c"),
          "uuid5"         -> IString("a55bb9b5-e633-5876-b34c-39c5ece44ba9")
        ))
    }
  }
}
