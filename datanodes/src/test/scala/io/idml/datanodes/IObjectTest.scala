package io.idml.datanodes

import io.idml.{IdmlNull, IdmlValue}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers
import scala.collection.mutable

/** Test the behaviour of the PObject class */
class IObjectTest extends AnyFunSuite with Matchers {

  // Equality
  test("empty == empty")(empty must equal(empty))
  test("primitives == primitives")(withPrimitives must equal(withPrimitives))
  test("nested objects == nested objects")(withNestedObjects must equal(withNestedObjects))
  test("nested arrays == nested arrays")(withNestedArrays must equal(withNestedArrays))

  test("empty != primitives")(IObject() must not equal withPrimitives)
  test("primitives != nested arrays")(withPrimitives must not equal withNestedArrays)
  test("nested objects != nested arrays")(withNestedObjects must not equal withNestedArrays)
  test("nested arrays != nested objects")(withNestedArrays must not equal withNestedObjects)

  def withPrimitives =
    new IObject(
      mutable.Map(
        "s" -> IdmlValue("abc"),
        "i" -> IdmlValue(123),
        "f" -> IdmlValue(123.4),
        "n" -> IdmlNull
      )
    )

  def withNestedObjects =
    new IObject(
      mutable.Map(
        "o" -> new IObject(
          mutable.Map(
            "s" -> IdmlValue("abc"),
            "i" -> IdmlValue(123),
            "f" -> IdmlValue(123.4),
            "n" -> IdmlNull
          )
        )
      )
    )

  def withNestedArrays =
    new IObject(
      mutable.Map(
        "a" -> new IArray(
          mutable.ListBuffer(
            IdmlValue("abc"),
            IdmlValue(123),
            IdmlValue(123.4),
            IdmlNull
          )
        )
      )
    )

  // deepmerge
  val inner1 = IObject("b" -> IInt(2))
  val inner2 = IObject("c" -> IInt(3))
  val outer1 = IObject("a" -> inner1)
  val outer2 = IObject("a" -> inner2)

  test("deepmerge should merge objects")(
    outer1.deepMerge(outer2) must equal(IObject("a" -> IObject("b" -> IInt(2), "c" -> IInt(3)))))

  val one = IInt(1)
  val two = IInt(2)
  val x   = (p: IdmlValue) => IObject("x" -> p)
  test("deepmerge should merge arrays") {
    x(IArray(one, two, one)) deepMerge x(IArray(two, two, one)) must equal(x(IArray(two, two, one)))
  }
  test("deepmerge should merge arrays where left is longer") {
    x(IArray(one, two, one, two)) deepMerge x(IArray(two, two, one)) must equal(
      x(IArray(two, two, one)))
  }
  test("deepmerge should merge arrays where right is longer") {
    x(IArray(one, two, one)) deepMerge x(IArray(two, two, one, one)) must equal(
      x(IArray(two, two, one, one)))
  }
  test("deepmerge should merge arrays with objects in") {
    x(IArray(x(one), x(two), x(one))) deepMerge x(IArray(x(two), x(two), x(one))) must equal(
      x(IArray(x(two), x(two), x(one))))
  }
}
