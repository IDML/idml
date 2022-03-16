package io.idml.jackson

import io.idml.datanodes.IDouble
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

class IDoubleParsingSpec extends AnyFunSuite with Matchers {
  import IdmlJackson.default._

  // Parsing
  test("parse(float min)")(pending) //(DataNodes.parse(Float.MinValue.toString) must equal(new PDouble(Float.MinValue)))
  test("parse(float max)")(pending) //(DataNodes.parse(Float.MaxValue.toString) must equal(new PDouble(Float.MaxValue)))
  test("parse(double min)")(parse(Double.MinValue.toString) must equal(new IDouble(Double.MinValue)))
  test("parse(double max)")(parse(Double.MaxValue.toString) must equal(new IDouble(Double.MaxValue)))

  // Generation
  test("generate(float min)")(pending) //(Float.MinValue.toString must equal(compact(new PDouble(Float.MinValue))))
  test("generate(float max)")(pending) //(Float.MaxValue.toString must equal(compact(new PDouble(Float.MaxValue))))
  test("generate(double min)")(Double.MinValue.toString must equal(compact(new IDouble(Double.MinValue))))
  test("generate(double max)")(Double.MaxValue.toString must equal(compact(new IDouble(Double.MaxValue))))
}
