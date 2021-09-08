package io.idml.datanodes

import io.idml.{IdmlNull, IdmlValue}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

/** Test the behaviour of the IdmlValue class */
class IdmlValueTest extends AnyFunSuite with Matchers {

  // Companion object
  test("companion - null")(IdmlValue(null) must equal(IdmlNull))
  test("companion - string")(IdmlValue("abc") must equal(IdmlValue("abc")))
  test("companion - true")(IdmlValue(v = true) must equal(ITrue))
  test("companion - false")(IdmlValue(v = false) must equal(IFalse))
  test("companion - long")(IdmlValue(v = Long.MaxValue) must equal(new IInt(Long.MaxValue)))
  test("companion - int")(IdmlValue(v = Int.MaxValue) must equal(new IInt(Int.MaxValue)))
  test("companion - float")(IdmlValue(v = Float.MaxValue) must equal(new IDouble(Float.MaxValue)))
  test("companion - double")(IdmlValue(v = Double.MaxValue - 1000) must equal(new IDouble(Double.MaxValue - 1000)))

}
