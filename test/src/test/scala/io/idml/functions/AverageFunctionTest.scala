package io.idml.functions

import io.idml.datanodes.{PArray, PDouble, PTrue}
import io.idml.{InvalidCaller, NoFields, PtolemyContext}
import org.scalatest.FunSuite

class AverageFunctionTest extends FunSuite {

  test("when given a missing field it leaves it untouched") {
    val ctx = new PtolemyContext(NoFields)
    AverageFunction.invoke(ctx)
    assert(ctx.cursor === NoFields)
  }

  test("when given a type it doesn't understand it returns InvalidCaller") {
    val ctx = new PtolemyContext(PTrue)
    AverageFunction.invoke(ctx)
    assert(ctx.cursor === InvalidCaller)
  }

  test("when given an empty array it returns InvalidCaller") {
    val ctx = new PtolemyContext(PArray())
    AverageFunction.invoke(ctx)
    assert(ctx.cursor === InvalidCaller)
  }

  test("when given an array of a single geo object it returns the first value") {
    val ctx = new PtolemyContext(PArray(PDouble(25)))
    AverageFunction.invoke(ctx)
    assert(ctx.cursor === PDouble(25))
  }

  test("when given a bounding box with equal points it outputs that point") {
    val ctx = new PtolemyContext(PArray(PDouble(25), PDouble(25), PDouble(25), PDouble(25)))
    AverageFunction.invoke(ctx)
    assert(ctx.cursor === PDouble(25))
  }

  test("when given a bounding box with non-equal points it outputs the average lat and long") {
    val ctx = new PtolemyContext(PArray(PDouble(-4.2392826), PDouble(-4.2392826), PDouble(-3.9925988), PDouble(-3.9925988)))
    AverageFunction.invoke(ctx)
    assert(ctx.cursor === PDouble(-4.1159407))
  }
}
