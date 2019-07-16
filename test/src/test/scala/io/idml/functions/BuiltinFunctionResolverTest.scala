package io.idml.functions

import io.idml.datanodes.PString
import io.idml.ast._
import org.scalatest.FunSuite

class BuiltinFunctionResolverTest extends FunSuite {

  val stringLiteral = ExecNavLiteral(Literal(PString("my_block")))
  val stringPipl    = Pipeline(List(stringLiteral))
  val stringArgs    = List(stringPipl)

  test("resolves apply(string)") {
    new BuiltinFunctionResolver().resolve("apply", stringArgs)
  }

  test("resolves applyArray(string)") {
    new BuiltinFunctionResolver().resolve("applyArray", stringArgs)
  }

  test("resolves array(string)") {
    new BuiltinFunctionResolver().resolve("array", stringArgs)
  }

  test("resolves extract(string)") {
    assert(new BuiltinFunctionResolver().resolve("extract", stringArgs) === Some(ExtractFunction(stringPipl)))
  }

  test("no matches") {
    assert(new BuiltinFunctionResolver().resolve("missing", Nil) === None)
    assert(new BuiltinFunctionResolver().resolve("missing", stringArgs) === None)
  }
}
