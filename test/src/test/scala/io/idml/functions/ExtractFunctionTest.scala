package io.idml.functions

import io.idml.datanodes._
import io.idml.ast.{ExecNavRelative, Field, Pipeline}
import io.idml.{IdmlContext, InvalidCaller, NoFields}
import org.scalatest.FunSuite

class ExtractFunctionTest extends FunSuite {

  def extract = ExtractFunction(Pipeline(List(ExecNavRelative, Field("a"))))

  test("extract returns the identical nothing when given nothing") {
    val ctx = new IdmlContext(NoFields)
    extract.invoke(ctx)
    assert(ctx.cursor === NoFields)
  }

  test("extract returns invalid caller if something that isn't an array is used") {
    val ctx = new IdmlContext(PString("abc"))
    extract.invoke(ctx)
    assert(ctx.cursor === InvalidCaller)
  }

  test("extract applies function to each element in an array") {
    val ctx =
      new IdmlContext(PArray(PObject("a" -> PTrue), PObject("a" -> PFalse)))
    extract.invoke(ctx)
    assert(ctx.cursor === PArray(PTrue, PFalse))
  }

  test("extract returns nothing if no results are returned") {
    val ctx =
      new IdmlContext(PArray(PObject("b" -> PTrue), PObject("b" -> PFalse)))
    extract.invoke(ctx)
    assert(ctx.cursor === NoFields)
  }

  test("extract filters out missing fields") {
    val ctx =
      new IdmlContext(PArray(PObject("b" -> PTrue), PObject("a" -> PFalse)))
    extract.invoke(ctx)
    assert(ctx.cursor === PArray(PFalse))
  }

}
