package io.idml.jackson

import io.idml.PtolemyNull
import org.scalatest.{FunSuite, MustMatchers}

class NullParsingSpec extends FunSuite with MustMatchers {
  import PtolemyJackson.default._
    // Parsing
  test("parse(null)")(parse("null") must equal(PtolemyNull))

  // Generation
  test("generate(null)")("null" must equal(compact(PtolemyNull)))

}
