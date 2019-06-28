package io.idml.circe

import io.idml.datanodes._
import org.scalatest.{MustMatchers, WordSpec}

class IdmlCirceSpec extends WordSpec with MustMatchers {

  "IdmlCirce" should {
    "work" in {
      PtolemyJson.parseUnsafe("""{"a":[1,2,3,"hello"]}""") must equal(
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
  }

}
