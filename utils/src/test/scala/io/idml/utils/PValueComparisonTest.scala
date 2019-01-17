package io.idml.utils

import io.idml.datanodes._
import org.scalatest.FunSuite
import org.scalatest.MustMatchers

class PValueComparisonTest extends FunSuite with MustMatchers {
  import io.idml.utils.PValueComparison._

  test("Accepts equal scalar values") {
    assertEqual(PString("a"), PString("a"))
    assertEqual(PInt(1), PInt(1))
    assertEqual(PDouble(123.456), PDouble(123.456))
    assertEqual(PTrue, PTrue)
  }

  test("Rejects different scalar values") {
    intercept[IllegalArgumentException](assertEqual(PString("a"), PString("b")))
    intercept[IllegalArgumentException](assertEqual(PInt(1), PInt(2)))
    intercept[IllegalArgumentException](assertEqual(PDouble(123.456), PDouble(789.101)))
    intercept[IllegalArgumentException](assertEqual(PTrue, PFalse))
  }

  test("Rejects different scalar types") {
    intercept[IllegalArgumentException](assertEqual(PString("1"), PInt(1)))
    intercept[IllegalArgumentException](assertEqual(PInt(1), PDouble(123.456)))
    intercept[IllegalArgumentException](assertEqual(PDouble(123.456), PFalse))
  }

  test("Accepts equal item arrays") {
    assertEqual(PArray(), PArray())
    assertEqual(PArray(PString("a")), PArray(PString("a")))
    assertEqual(PArray(PInt(1)), PArray(PInt(1)))
    assertEqual(PArray(PDouble(123.456)), PArray(PDouble(123.456)))
    assertEqual(PArray(PTrue), PArray(PTrue))
  }

  test("Rejects arrays with different items") {
    intercept[IllegalArgumentException](assertEqual(PArray(), PArray(PString("a"))))
    intercept[IllegalArgumentException](assertEqual(PArray(PString("a")), PArray(PString("b"))))
    intercept[IllegalArgumentException](assertEqual(PArray(PInt(1)), PArray(PInt(2))))
    intercept[IllegalArgumentException](assertEqual(PArray(PDouble(123.456)), PArray(PDouble(789.101))))
    intercept[IllegalArgumentException](assertEqual(PArray(PTrue), PArray(PFalse)))
  }

  test("Rejects arrays with different cardinality") {
    intercept[IllegalArgumentException](assertEqual(PArray(PString("a"), PString("a")), PArray(PString("a"))))
    intercept[IllegalArgumentException](assertEqual(PArray(PString("a")), PArray(PString("a"), PString("a"))))
  }

  test("Accepts equal objects") {
    assertEqual(PObject(), PObject())
    assertEqual(PObject("a" -> PString("a")), PObject("a"     -> PString("a")))
    assertEqual(PObject("a" -> PInt(1)), PObject("a"          -> PInt(1)))
    assertEqual(PObject("a" -> PDouble(123.456)), PObject("a" -> PDouble(123.456)))
    assertEqual(PObject("a" -> PTrue), PObject("a"            -> PTrue))
  }

  test("Rejects objects with different values") {
    intercept[IllegalArgumentException](assertEqual(PObject(), PObject("a" -> PString("b"))))
    intercept[IllegalArgumentException](assertEqual(PObject("a"            -> PString("a")), PObject("a" -> PString("b"))))
    intercept[IllegalArgumentException](assertEqual(PObject("a"            -> PInt(1)), PObject("a" -> PArray(PInt(2)))))
    intercept[IllegalArgumentException](assertEqual(PObject("a"            -> PDouble(123.456)), PObject("a" -> PDouble(789.101))))
    intercept[IllegalArgumentException](assertEqual(PObject("a"            -> PTrue), PObject("a" -> PFalse)))
  }

  test("Rejects objects with different keys") {
    intercept[IllegalArgumentException](assertEqual(PObject(), PObject("a" -> PString("b"))))
    intercept[IllegalArgumentException](assertEqual(PObject("a"            -> PString("a")), PObject("b" -> PString("b"))))
    intercept[IllegalArgumentException](assertEqual(PObject("a"            -> PInt(1)), PObject("b" -> PArray(PInt(2)))))
    intercept[IllegalArgumentException](assertEqual(PObject("a"            -> PDouble(123.456)), PObject("b" -> PDouble(789.101))))
    intercept[IllegalArgumentException](assertEqual(PObject("a"            -> PTrue), PObject("b" -> PFalse)))
  }
}
