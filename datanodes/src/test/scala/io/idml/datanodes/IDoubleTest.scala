package io.idml.datanodes

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

/** Test the behaviour of the PDouble class */
class IDoubleTest extends AnyFunSuite with Matchers {
  // Equality
  test("float min == min")(new IDouble(Float.MinValue) must equal(new IDouble(Float.MinValue)))
  test("float max == max")(new IDouble(Float.MaxValue) must equal(new IDouble(Float.MaxValue)))
  test("float max != min")(new IDouble(Float.MaxValue) must not equal new IDouble(Float.MinValue))
  test("double min == min")(new IDouble(Double.MinValue) must equal(new IDouble(Double.MinValue)))
  test("double max == max")(new IDouble(Double.MaxValue) must equal(new IDouble(Double.MaxValue)))
  test("double max != min")(
    new IDouble(Double.MaxValue) must not equal new IDouble(Double.MinValue))

  test("float = long")(new IDouble(1000f) must equal(new IDouble(1000L)))
  test("double = long")(new IDouble(1000.0) must equal(new IDouble(1000L)))

  // Comparison with other numerical types
  test("PDouble(int) == PInt(int)")(new IDouble(1000) must equal(new IInt(1000)))
  test("PDouble(long) == PInt(long)")(new IDouble(1000L) must equal(new IInt(1000L)))
  test("PDouble(float) == PInt(int)")(new IDouble(1000.0) must equal(new IInt(1000)))
  test("PDouble(double) == PInt(int)")(new IDouble(1000f) must equal(new IInt(1000)))

}
