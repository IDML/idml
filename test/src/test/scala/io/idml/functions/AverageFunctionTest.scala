package io.idml.functions

import io.idml.datanodes.{IArray, IDouble, ITrue}
import io.idml.{IdmlContext, InvalidCaller, NoFields}
import org.scalatest.funsuite.AnyFunSuite

class AverageFunctionTest extends AnyFunSuite {

  test("when given a missing field it leaves it untouched") {
    val ctx = new IdmlContext(NoFields)
    AverageFunction.invoke(ctx)
    assert(ctx.cursor === NoFields)
  }

  test("when given a type it doesn't understand it returns InvalidCaller") {
    val ctx = new IdmlContext(ITrue)
    AverageFunction.invoke(ctx)
    assert(ctx.cursor === InvalidCaller)
  }

  test("when given an empty array it returns InvalidCaller") {
    val ctx = new IdmlContext(IArray())
    AverageFunction.invoke(ctx)
    assert(ctx.cursor === InvalidCaller)
  }

  test("when given an array of a single geo object it returns the first value") {
    val ctx = new IdmlContext(IArray(IDouble(25)))
    AverageFunction.invoke(ctx)
    assert(ctx.cursor === IDouble(25))
  }

  test("when given a bounding box with equal points it outputs that point") {
    val ctx = new IdmlContext(IArray(IDouble(25), IDouble(25), IDouble(25), IDouble(25)))
    AverageFunction.invoke(ctx)
    assert(ctx.cursor === IDouble(25))
  }

  test("when given a bounding box with non-equal points it outputs the average lat and long") {
    val ctx = new IdmlContext(IArray(IDouble(-4.2392826), IDouble(-4.2392826), IDouble(-3.9925988), IDouble(-3.9925988)))
    AverageFunction.invoke(ctx)
    assert(ctx.cursor === IDouble(-4.1159407))
  }
}
