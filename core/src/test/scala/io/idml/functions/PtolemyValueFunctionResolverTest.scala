package io.idml.functions

import io.idml.datanodes.PString
import io.idml.PtolemyValue
import io.idml.ast.{ExecNavLiteral, Literal, Pipeline}
import org.scalatest.FunSuite

class PtolemyValueFunctionResolverTest extends FunSuite {

  val pv = classOf[PtolemyValue]

  val pipl = Pipeline(List(ExecNavLiteral(Literal(PString("")))))

  test("return none if a method can't be found") {
    assert(new PtolemyValueFunctionResolver().resolve("missing", Nil) === None)
  }

  test("return a function if a 0-arity method can be found") {
    assert(new PtolemyValueFunctionResolver().resolve("int", Nil) === Some(PtolemyValueFunction(pv.getMethod("int"), Nil)))
  }

  test("return a function if a 1-arity function can be found") {
    assert(
      new PtolemyValueFunctionResolver()
        .resolve("default", List(pipl)) === Some(
        PtolemyValueFunction(pv.getMethod("default", classOf[PtolemyValue]), List(pipl), isNAry = false)))
  }

  test("return a function if a 2-arity function can be found") {
    assert(
      new PtolemyValueFunctionResolver()
        .resolve("slice", List(pipl, pipl)) === Some(
        PtolemyValueFunction(pv.getMethod("slice", classOf[PtolemyValue], classOf[PtolemyValue]), List(pipl, pipl), isNAry = false)))
  }
}
