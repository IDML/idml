package io.idml.utils

import io.idml.datanodes._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must

class PValueComparisonTest extends AnyFunSuite with must.Matchers {
  import io.idml.utils.PValueComparison._

  test("Accepts equal scalar values") {
    assertEqual(IString("a"), IString("a"))
    assertEqual(IInt(1), IInt(1))
    assertEqual(IDouble(123.456), IDouble(123.456))
    assertEqual(ITrue, ITrue)
  }

  test("Rejects different scalar values") {
    intercept[IllegalArgumentException](assertEqual(IString("a"), IString("b")))
    intercept[IllegalArgumentException](assertEqual(IInt(1), IInt(2)))
    intercept[IllegalArgumentException](assertEqual(IDouble(123.456), IDouble(789.101)))
    intercept[IllegalArgumentException](assertEqual(ITrue, IFalse))
  }

  test("Rejects different scalar types") {
    intercept[IllegalArgumentException](assertEqual(IString("1"), IInt(1)))
    intercept[IllegalArgumentException](assertEqual(IInt(1), IDouble(123.456)))
    intercept[IllegalArgumentException](assertEqual(IDouble(123.456), IFalse))
  }

  test("Accepts equal item arrays") {
    assertEqual(IArray(), IArray())
    assertEqual(IArray(IString("a")), IArray(IString("a")))
    assertEqual(IArray(IInt(1)), IArray(IInt(1)))
    assertEqual(IArray(IDouble(123.456)), IArray(IDouble(123.456)))
    assertEqual(IArray(ITrue), IArray(ITrue))
  }

  test("Rejects arrays with different items") {
    intercept[IllegalArgumentException](assertEqual(IArray(), IArray(IString("a"))))
    intercept[IllegalArgumentException](assertEqual(IArray(IString("a")), IArray(IString("b"))))
    intercept[IllegalArgumentException](assertEqual(IArray(IInt(1)), IArray(IInt(2))))
    intercept[IllegalArgumentException](assertEqual(IArray(IDouble(123.456)), IArray(IDouble(789.101))))
    intercept[IllegalArgumentException](assertEqual(IArray(ITrue), IArray(IFalse)))
  }

  test("Rejects arrays with different cardinality") {
    intercept[IllegalArgumentException](assertEqual(IArray(IString("a"), IString("a")), IArray(IString("a"))))
    intercept[IllegalArgumentException](assertEqual(IArray(IString("a")), IArray(IString("a"), IString("a"))))
  }

  test("Accepts equal objects") {
    assertEqual(IObject(), IObject())
    assertEqual(IObject("a" -> IString("a")), IObject("a"     -> IString("a")))
    assertEqual(IObject("a" -> IInt(1)), IObject("a"          -> IInt(1)))
    assertEqual(IObject("a" -> IDouble(123.456)), IObject("a" -> IDouble(123.456)))
    assertEqual(IObject("a" -> ITrue), IObject("a"            -> ITrue))
  }

  test("Rejects objects with different values") {
    intercept[IllegalArgumentException](assertEqual(IObject(), IObject("a" -> IString("b"))))
    intercept[IllegalArgumentException](assertEqual(IObject("a"            -> IString("a")), IObject("a" -> IString("b"))))
    intercept[IllegalArgumentException](assertEqual(IObject("a"            -> IInt(1)), IObject("a" -> IArray(IInt(2)))))
    intercept[IllegalArgumentException](assertEqual(IObject("a"            -> IDouble(123.456)), IObject("a" -> IDouble(789.101))))
    intercept[IllegalArgumentException](assertEqual(IObject("a"            -> ITrue), IObject("a" -> IFalse)))
  }

  test("Rejects objects with different keys") {
    intercept[IllegalArgumentException](assertEqual(IObject(), IObject("a" -> IString("b"))))
    intercept[IllegalArgumentException](assertEqual(IObject("a"            -> IString("a")), IObject("b" -> IString("b"))))
    intercept[IllegalArgumentException](assertEqual(IObject("a"            -> IInt(1)), IObject("b" -> IArray(IInt(2)))))
    intercept[IllegalArgumentException](assertEqual(IObject("a"            -> IDouble(123.456)), IObject("b" -> IDouble(789.101))))
    intercept[IllegalArgumentException](assertEqual(IObject("a"            -> ITrue), IObject("b" -> IFalse)))
  }
}
