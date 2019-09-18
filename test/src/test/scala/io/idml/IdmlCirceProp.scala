package io.idml

import com.fasterxml.jackson.core.JsonParseException
import io.circe.Json
import io.circe.testing.ArbitraryInstances
import io.idml.circe.IdmlCirce
import io.idml.jackson.IdmlJackson
import org.scalacheck._
import org.scalacheck.Prop.forAll

import scala.util.Try

class IdmlJsonProperties extends Properties("IdmlJson") with ArbitraryInstances {

  property("IdmlCirce should be able to parse anything IdmlJackson can") = forAll { j: Json =>
    val str           = j.noSpaces
    val jacksonResult = IdmlJackson.default.parseEither(str)
    val circeResult   = IdmlCirce.parseEither(str)
    (jacksonResult, circeResult) match {
      case (Right(jr), Right(cr)) =>
        jr == cr
      case (Right(_), Left(_)) =>
        false
      case (Left(_), _) =>
        //true // this JSON is a bit hard for jackson, so we'll give it some leeway
        // NOPE
        //false
        true
    }
  }
}
