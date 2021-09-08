package io.idml.functions

import io.idml.datanodes.IString
import io.idml.IdmlValue
import io.idml.ast.{ExecNavLiteral, Literal, Pipeline}
import org.scalatest.funsuite.AnyFunSuite

class IdmlValueFunctionResolverTest extends AnyFunSuite {

  val pv = classOf[IdmlValue]

  val pipl = Pipeline(List(ExecNavLiteral(Literal(IString("")))))

  test("return none if a method can't be found") {
    assert(new IdmlValueFunctionResolver().resolve("missing", Nil) === None)
  }

  test("return a function if a 0-arity method can be found") {
    assert(new IdmlValueFunctionResolver().resolve("int", Nil) === Some(IdmlValueFunction(pv.getMethod("int"), Nil)))
  }

  test("return a function if a 1-arity function can be found") {
    assert(new IdmlValueFunctionResolver()
      .resolve("default", List(pipl)) === Some(IdmlValueFunction(pv.getMethod("default", classOf[IdmlValue]), List(pipl), isNAry = false)))
  }

  test("return a function if a 2-arity function can be found") {
    assert(
      new IdmlValueFunctionResolver()
        .resolve("slice", List(pipl, pipl)) === Some(
        IdmlValueFunction(pv.getMethod("slice", classOf[IdmlValue], classOf[IdmlValue]), List(pipl, pipl), isNAry = false)))
  }
}
