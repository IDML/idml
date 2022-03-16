package io.idml.functions

import io.idml.datanodes._
import io.idml.ast.{ExecNavRelative, Field, Pipeline}
import io.idml.{IdmlContext, InvalidCaller, NoFields}
import org.scalatest.funsuite.AnyFunSuite

class ExtractFunctionTest extends AnyFunSuite {

  def extract = ExtractFunction(Pipeline(List(ExecNavRelative, Field("a"))))

  test("extract returns the identical nothing when given nothing") {
    val ctx = new IdmlContext(NoFields)
    extract.invoke(ctx)
    assert(ctx.cursor === NoFields)
  }

  test("extract returns invalid caller if something that isn't an array is used") {
    val ctx = new IdmlContext(IString("abc"))
    extract.invoke(ctx)
    assert(ctx.cursor === InvalidCaller)
  }

  test("extract applies function to each element in an array") {
    val ctx =
      new IdmlContext(IArray(IObject("a" -> ITrue), IObject("a" -> IFalse)))
    extract.invoke(ctx)
    assert(ctx.cursor === IArray(ITrue, IFalse))
  }

  test("extract returns nothing if no results are returned") {
    val ctx =
      new IdmlContext(IArray(IObject("b" -> ITrue), IObject("b" -> IFalse)))
    extract.invoke(ctx)
    assert(ctx.cursor === NoFields)
  }

  test("extract filters out missing fields") {
    val ctx =
      new IdmlContext(IArray(IObject("b" -> ITrue), IObject("a" -> IFalse)))
    extract.invoke(ctx)
    assert(ctx.cursor === IArray(IFalse))
  }

}
