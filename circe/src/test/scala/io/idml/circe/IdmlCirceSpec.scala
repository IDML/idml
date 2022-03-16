package io.idml.circe

import io.idml.datanodes._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import io.circe.syntax._
import io.idml.circe.instances._

class IdmlCirceSpec extends AnyWordSpec with Matchers {

  "IdmlCirce" should {
    "work" in {
      IdmlCirce.parse("""{"a":[1,2,3,"hello"]}""") must equal(
        IObject(
          "a" -> IArray(
            IInt(1),
            IInt(2),
            IInt(3),
            IString("hello")
          )
        )
      )
    }
    "preserve key ordering" in {
      IObject(
        "a" -> IInt(1),
        "c" -> IInt(3),
        "b" -> IInt(2),
        "Z" -> IInt(0)
      ).asJson.noSpaces must equal(
        """{"Z":0,"a":1,"b":2,"c":3}"""
      )
    }
  }

}
