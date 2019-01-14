package io.idml.datanodes

import io.idml.{PtolemyNull, PtolemyValue}
import org.scalatest._

/** Test the behaviour of the PtolemyValue class */
class PtolemyValueTest extends FunSuite with MustMatchers {

  // Companion object
  test("companion - null")(PtolemyValue(null) must equal(PtolemyNull))
  test("companion - string")(PtolemyValue("abc") must equal(PtolemyValue("abc")))
  test("companion - true")(PtolemyValue(v = true) must equal(PTrue))
  test("companion - false")(PtolemyValue(v = false) must equal(PFalse))
  test("companion - long")(PtolemyValue(v = Long.MaxValue) must equal(new PInt(Long.MaxValue)))
  test("companion - int")(PtolemyValue(v = Int.MaxValue) must equal(new PInt(Int.MaxValue)))
  test("companion - float")(PtolemyValue(v = Float.MaxValue) must equal(new PDouble(Float.MaxValue)))
  test("companion - double")(PtolemyValue(v = Double.MaxValue - 1000) must equal(new PDouble(Double.MaxValue - 1000)))

}
