package io.idml.jackson

import io.idml.{Ptolemy, PtolemyValue}
import io.idml.datanodes.{PInt, PObject, PString}
import org.scalatest.{MustMatchers, WordSpec}

class JacksonFunctionsSpec extends WordSpec with MustMatchers {

  "JacksonFunctions" should {
    val jf = new JacksonFunctions
    "serialize objects" in {
      jf.obj.serialize(PObject("v" -> PInt(123))) must equal(PString("""{"v":123}"""))
    }
    "parse objects" in {
      jf.obj.parseJson(PString("""{"v":123}""")) must equal(PObject("v" -> PInt(123)))
    }
    "be loaded automatically" in {
      val p = new Ptolemy()
      val program = p.fromString(
        """
          |uuid3 = root.uuid3()
          |uuid5 = root.uuid5()
          |object = js.parseJson()
          |alsoObject = {"v":123}.serialize()
          |number = root.random()
          |anotherNumber = root.random(1, 100)
          |""".stripMargin)
      val result = program.run(PObject("hello" -> PtolemyValue("world"), "js" -> PString("{\"hello\" -> \"world\"}")))
      println(result)
          /*
      PtolemyJackson.default.compact(result) must equal(
        PtolemyJackson.default.compact(PObject(
          "object" -> PObject("v" -> PInt(123)),
          "alsoObject" -> PString("""{"v":123}"""),
          "anotherNumber" -> PInt(18),
          "number" -> PInt(-863086648761741499L),
          "uuid3" -> PString("fbc24bcc-7a17-3475-8fc1-327fcfebdaf6"),
          "uuid5" -> PString("2248ee2f-a0aa-5ad9-9178-531f924bf00b")
        )
      ))
      */
    }
  }

}
