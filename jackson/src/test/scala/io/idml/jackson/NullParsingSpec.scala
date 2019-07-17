package io.idml.jackson

import io.idml.IdmlNull
import org.scalatest.{FunSuite, MustMatchers}

class NullParsingSpec extends FunSuite with MustMatchers {
  import IdmlJackson.default._
  // Parsing
  test("parse(null)")(parse("null") must equal(IdmlNull))

  // Generation
  test("generate(null)")("null" must equal(compact(IdmlNull)))

}
