package io.idml.datanodes

import io.idml._
import org.scalatest._

import scala.collection.mutable

/** Test the behaviour of the PArray class */
class PArrayTest extends FunSuite with MustMatchers {

  // Equality
  test("empty == empty")(empty must equal(empty))
  test("primitives == primitives")(withPrimitives must equal(withPrimitives))
  test("nested objects == nested objects")(withNestedObjects must equal(withNestedObjects))
  test("nested arrays == nested arrays")(withNestedArrays must equal(withNestedArrays))

  test("empty != primitives")(PObject() must not equal withPrimitives)
  test("primitives != nested arrays")(withPrimitives must not equal withNestedArrays)
  test("nested objects != nested arrays")(withNestedObjects must not equal withNestedArrays)
  test("nested arrays != nested objects")(withNestedArrays must not equal withNestedObjects)

  // empty
  test("empty things should say they're empty")(PArray().empty() must equal(PBool(true)))
  test("non-empty things should not say they're empty")(PArray(PInt(1)).empty() must equal(PBool(false)))

  // combinations
  test("combinations should work")(
    PArray(PInt(1), PInt(2), PInt(3)).combinations(PInt(2))
      must equal(PArray(PArray(PInt(1), PInt(2)), PArray(PInt(1), PInt(3)), PArray(PInt(2), PInt(3)))))

  // flatten
  test("array flatten")(PArray(PArray(PInt(1)), PArray(PInt(2))).flatten() must equal(PArray(PInt(1), PInt(2))))
  test("array flattening on an uneven array")(
    PArray(PArray(PInt(1), PInt(2)), PInt(3)).flatten() must equal(PArray(PInt(1), PInt(2), PInt(3))))

  // combineAll
  test("array combineAll")(
    PArray(PArray(PInt(1)), PArray(PInt(2)), PArray(PInt(3))).combineAll() must equal(PArray(PInt(1), PInt(2), PInt(3))))
  test("object combineAll")(
    PArray(PObject("a" -> PInt(1)), PObject("b" -> PInt(2)), PObject("a" -> PInt(3))).combineAll() must equal(
      PObject("a" -> PInt(3), "b" -> PInt(2))))
  test("string combineAll")(PArray(PString("hello"), PString("world")).combineAll() must equal(PString("helloworld")))
  test("int combineAll")(PArray(PInt(1), PInt(2), PInt(3), PInt(4)).combineAll() must equal(PInt(10)))
  test("double combineAll")(PArray(PDouble(1.1), PDouble(2.1)).combineAll() must equal(PDouble(3.2)))
  test("combineAll should fail on an array with multiple types")(PArray(PString("hello"), PInt(2)).combineAll() must equal(InvalidCaller))
  test("combineAll should work with a nothing")(
    PArray(PString("hello"), Filtered, PString("world")).combineAll must equal(PString("helloworld")))

  // Test data
  def withPrimitives =
    new PArray(
      mutable.ListBuffer(
        IdmlValue("abc"),
        IdmlValue(123),
        IdmlValue(123.4),
        IdmlNull
      ))

  def withNestedObjects =
    new PArray(
      mutable.ListBuffer(
        new PObject(
          mutable.SortedMap(
            "s" -> IdmlValue("abc"),
            "i" -> IdmlValue(123),
            "f" -> IdmlValue(123.4),
            "n" -> IdmlNull
          ))))

  def withNestedArrays = new PArray(mutable.ListBuffer(withPrimitives))

}
