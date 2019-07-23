package io.idml.datanodes

import io.idml._
import org.scalatest._

import scala.collection.mutable

/** Test the behaviour of the PArray class */
class IArrayTest extends FunSuite with MustMatchers {

  // Equality
  test("empty == empty")(empty must equal(empty))
  test("primitives == primitives")(withPrimitives must equal(withPrimitives))
  test("nested objects == nested objects")(withNestedObjects must equal(withNestedObjects))
  test("nested arrays == nested arrays")(withNestedArrays must equal(withNestedArrays))

  test("empty != primitives")(IObject() must not equal withPrimitives)
  test("primitives != nested arrays")(withPrimitives must not equal withNestedArrays)
  test("nested objects != nested arrays")(withNestedObjects must not equal withNestedArrays)
  test("nested arrays != nested objects")(withNestedArrays must not equal withNestedObjects)

  // empty
  test("empty things should say they're empty")(IArray().empty() must equal(IBool(true)))
  test("non-empty things should not say they're empty")(IArray(IInt(1)).empty() must equal(IBool(false)))

  // combinations
  test("combinations should work")(
    IArray(IInt(1), IInt(2), IInt(3)).combinations(IInt(2))
      must equal(IArray(IArray(IInt(1), IInt(2)), IArray(IInt(1), IInt(3)), IArray(IInt(2), IInt(3)))))

  // flatten
  test("array flatten")(IArray(IArray(IInt(1)), IArray(IInt(2))).flatten() must equal(IArray(IInt(1), IInt(2))))
  test("array flattening on an uneven array")(
    IArray(IArray(IInt(1), IInt(2)), IInt(3)).flatten() must equal(IArray(IInt(1), IInt(2), IInt(3))))

  // combineAll
  test("array combineAll")(
    IArray(IArray(IInt(1)), IArray(IInt(2)), IArray(IInt(3))).combineAll() must equal(IArray(IInt(1), IInt(2), IInt(3))))
  test("object combineAll")(
    IArray(IObject("a" -> IInt(1)), IObject("b" -> IInt(2)), IObject("a" -> IInt(3))).combineAll() must equal(
      IObject("a" -> IInt(3), "b" -> IInt(2))))
  test("string combineAll")(IArray(IString("hello"), IString("world")).combineAll() must equal(IString("helloworld")))
  test("int combineAll")(IArray(IInt(1), IInt(2), IInt(3), IInt(4)).combineAll() must equal(IInt(10)))
  test("double combineAll")(IArray(IDouble(1.1), IDouble(2.1)).combineAll() must equal(IDouble(3.2)))
  test("combineAll should fail on an array with multiple types")(IArray(IString("hello"), IInt(2)).combineAll() must equal(InvalidCaller))
  test("combineAll should work with a nothing")(
    IArray(IString("hello"), Filtered, IString("world")).combineAll must equal(IString("helloworld")))

  // Test data
  def withPrimitives =
    new IArray(
      mutable.ListBuffer(
        IdmlValue("abc"),
        IdmlValue(123),
        IdmlValue(123.4),
        IdmlNull
      ))

  def withNestedObjects =
    new IArray(
      mutable.ListBuffer(
        new IObject(
          mutable.Map(
            "s" -> IdmlValue("abc"),
            "i" -> IdmlValue(123),
            "f" -> IdmlValue(123.4),
            "n" -> IdmlNull
          ))))

  def withNestedArrays = new IArray(mutable.ListBuffer(withPrimitives))

}
