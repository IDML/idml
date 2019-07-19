package io.idml.jackson

import io.idml.IdmlJson
import io.idml.datanodes.IString
import org.scalatest.{FunSuite, MustMatchers}

class StringParsingSpec extends FunSuite with MustMatchers {
  import IdmlJackson.default._

  // Parsing
  test("parse string")(parse("\"a string\"") must equal(new IString("a string")))

  // Generation
  test("generate string")("\"a string\"" must equal(compact(new IString("a string"))))
}
