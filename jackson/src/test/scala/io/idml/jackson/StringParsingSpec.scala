package io.idml.jackson

import io.idml.IdmlJson
import io.idml.datanodes.IString
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

class StringParsingSpec extends AnyFunSuite with Matchers {
  import IdmlJackson.default._

  // Parsing
  test("parse string")(parse("\"a string\"") must equal(new IString("a string")))

  // Generation
  test("generate string")("\"a string\"" must equal(compact(new IString("a string"))))
}
