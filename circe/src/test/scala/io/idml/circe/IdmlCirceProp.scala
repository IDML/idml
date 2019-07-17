package io.idml.circe

import io.circe.testing.ArbitraryInstances
import org.scalacheck.Properties
import org.scalacheck.Prop.forAll
import io.circe._

import scala.util.Try

class IdmlCirceProp extends Properties("IdmlCirce") with ArbitraryInstances {

  /*
  property("is identical to the jackson parser") = forAll { j: Json =>
    val str = j.noSpaces
    Try {
      IdmlJson.parseUnsafe(str) == jackson.IdmlJson.parse(str)
    }.recover {
        case _: JsonParseException => true
      }
      .getOrElse(false)
  }
 */
}
