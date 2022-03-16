package io.idml.functions

import io.idml.datanodes.IString
import io.idml.IdmlValue
import io.idml.ast.{ExecNavLiteral, Literal, Pipeline}
import org.scalatest.funsuite.AnyFunSuite

/** Verify the behaviour of IdmlValueFunction */
class IdmlValueFunctionTest extends AnyFunSuite {

  val pv = classOf[IdmlValue]

  val pipl = Pipeline(List(ExecNavLiteral(Literal(IString("")))))

  test("ensure we don't support methods that don't return PValues") {
    intercept[IllegalArgumentException](IdmlValueFunction(pv.getMethod("toString"), Nil))
  }

  test("ensure we support methods that return PValues") {
    IdmlValueFunction(pv.getMethod("int"), Nil)
  }

  test("ensure we support methods which accept PValues as parameters") {
    IdmlValueFunction(pv.getMethod("slice", classOf[IdmlValue], classOf[IdmlValue]), List(pipl, pipl))
  }

  test("ensure we don't support methods that don't accept PValues") {
    intercept[IllegalArgumentException](
      IdmlValueFunction(pv.getMethod("slice", classOf[Option[Int]], classOf[Option[Int]]), List(pipl, pipl)))
  }

  test("ensure arg count and method arity match - int() with 1 extra argument") {
    intercept[IllegalArgumentException](IdmlValueFunction(pv.getMethod("int"), List(pipl)))
  }

  test("ensure arg count and method arity match - slice(f,t) with only 1 argument") {
    intercept[IllegalArgumentException](IdmlValueFunction(pv.getMethod("slice", classOf[IdmlValue], classOf[IdmlValue]), List(pipl)))
  }

  test("evaluate arguments for the current context") {
    pending
  }

  test("invoke the method with the evaluated argument results") {
    pending
  }

  test("update context cursor with new value") {
    pending
  }

}
