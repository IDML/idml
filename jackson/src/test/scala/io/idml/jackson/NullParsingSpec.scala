package io.idml.jackson

import io.idml.IdmlNull
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

class NullParsingSpec extends AnyFunSuite with Matchers {
  import IdmlJackson.default._
  // Parsing
  test("parse(null)")(parse("null") must equal(IdmlNull))

  // Generation
  test("generate(null)")("null" must equal(compact(IdmlNull)))

}
