package io.idml.datanodes

import io.idml.PtolemyJson
import org.scalatest._

/** Test the behaviour of the PDouble class */
class PDoubleTest extends FunSuite with MustMatchers {
  import PtolemyJson._

  // Parsing
  test("parse(float min)")(pending) //(DataNodes.parse(Float.MinValue.toString) must equal(new PDouble(Float.MinValue)))
  test("parse(float max)")(pending) //(DataNodes.parse(Float.MaxValue.toString) must equal(new PDouble(Float.MaxValue)))
  test("parse(double min)")(PtolemyJson.parse(Double.MinValue.toString) must equal(new PDouble(Double.MinValue)))
  test("parse(double max)")(PtolemyJson.parse(Double.MaxValue.toString) must equal(new PDouble(Double.MaxValue)))

  // Generation
  test("generate(float min)")(pending) //(Float.MinValue.toString must equal(compact(new PDouble(Float.MinValue))))
  test("generate(float max)")(pending) //(Float.MaxValue.toString must equal(compact(new PDouble(Float.MaxValue))))
  test("generate(double min)")(Double.MinValue.toString must equal(compact(new PDouble(Double.MinValue))))
  test("generate(double max)")(Double.MaxValue.toString must equal(compact(new PDouble(Double.MaxValue))))

  // Equality
  test("float min == min")(new PDouble(Float.MinValue) must equal(new PDouble(Float.MinValue)))
  test("float max == max")(new PDouble(Float.MaxValue) must equal(new PDouble(Float.MaxValue)))
  test("float max != min")(new PDouble(Float.MaxValue) must not equal new PDouble(Float.MinValue))
  test("double min == min")(new PDouble(Double.MinValue) must equal(new PDouble(Double.MinValue)))
  test("double max == max")(new PDouble(Double.MaxValue) must equal(new PDouble(Double.MaxValue)))
  test("double max != min")(new PDouble(Double.MaxValue) must not equal new PDouble(Double.MinValue))

  test("float = long")(new PDouble(1000f) must equal(new PDouble(1000L)))
  test("double = long")(new PDouble(1000.0) must equal(new PDouble(1000L)))

  // Comparison with other numerical types
  test("PDouble(int) == PInt(int)")(new PDouble(1000) must equal(new PInt(1000)))
  test("PDouble(long) == PInt(long)")(new PDouble(1000L) must equal(new PInt(1000L)))
  test("PDouble(float) == PInt(int)")(new PDouble(1000.0) must equal(new PInt(1000)))
  test("PDouble(double) == PInt(int)")(new PDouble(1000f) must equal(new PInt(1000)))

}
