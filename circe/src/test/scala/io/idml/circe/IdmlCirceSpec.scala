package io.idml.circe

import io.idml.datanodes._
import org.scalatest.{MustMatchers, WordSpec}
import io.circe.syntax._
import io.idml.circe.instances._

class IdmlCirceSpec extends WordSpec with MustMatchers {

  "IdmlCirce" should {
    "work" in {
      PtolemyCirce.parse("""{"a":[1,2,3,"hello"]}""") must equal(
        PObject(
          "a" -> PArray(
            PInt(1),
            PInt(2),
            PInt(3),
            PString("hello")
          )
        )
      )
    }
    "preserve key ordering" in {
      PObject(
        "a" -> PInt(1),
        "c" -> PInt(3),
        "b" -> PInt(2),
        "Z" -> PInt(0)
      ).asJson.noSpaces must equal(
        """{"Z":0,"a":1,"b":2,"c":3}"""
      )
    }
  }

}
