package io.idml.jackson

import io.idml.datanodes.PDouble
import org.scalatest.{FunSuite, MustMatchers}

class PDoubleParsingSpec extends FunSuite with MustMatchers {
  import PtolemyJackson.default._

  // Parsing
  test("parse(float min)")(pending) //(DataNodes.parse(Float.MinValue.toString) must equal(new PDouble(Float.MinValue)))
  test("parse(float max)")(pending) //(DataNodes.parse(Float.MaxValue.toString) must equal(new PDouble(Float.MaxValue)))
  test("parse(double min)")(parse(Double.MinValue.toString) must equal(new PDouble(Double.MinValue)))
  test("parse(double max)")(parse(Double.MaxValue.toString) must equal(new PDouble(Double.MaxValue)))

  // Generation
  test("generate(float min)")(pending) //(Float.MinValue.toString must equal(compact(new PDouble(Float.MinValue))))
  test("generate(float max)")(pending) //(Float.MaxValue.toString must equal(compact(new PDouble(Float.MaxValue))))
  test("generate(double min)")(Double.MinValue.toString must equal(compact(new PDouble(Double.MinValue))))
  test("generate(double max)")(Double.MaxValue.toString must equal(compact(new PDouble(Double.MaxValue))))
}
