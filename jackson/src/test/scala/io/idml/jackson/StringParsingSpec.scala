package io.idml.jackson

import io.idml.PtolemyJson
import io.idml.datanodes.PString
import org.scalatest.{FunSuite, MustMatchers}

class StringParsingSpec extends FunSuite with MustMatchers {
  import PtolemyJackson.default._

  // Parsing
  test("parse string")(parse("\"a string\"") must equal(new PString("a string")))

  // Generation
  test("generate string")("\"a string\"" must equal(compact(new PString("a string"))))
}
