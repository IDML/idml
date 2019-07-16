package io.idml.functions

import io.idml.datanodes.PString
import io.idml.PtolemyValue
import io.idml.ast.{ExecNavLiteral, Literal, Pipeline}
import org.scalatest.FunSuite

/** Verify the behaviour of PtolemyValueFunction */
class PtolemyValueFunctionTest extends FunSuite {

  val pv = classOf[PtolemyValue]

  val pipl = Pipeline(List(ExecNavLiteral(Literal(PString("")))))

  test("ensure we don't support methods that don't return PValues") {
    intercept[IllegalArgumentException](PtolemyValueFunction(pv.getMethod("toString"), Nil))
  }

  test("ensure we support methods that return PValues") {
    PtolemyValueFunction(pv.getMethod("int"), Nil)
  }

  test("ensure we support methods which accept PValues as parameters") {
    PtolemyValueFunction(pv.getMethod("slice", classOf[PtolemyValue], classOf[PtolemyValue]), List(pipl, pipl))
  }

  test("ensure we don't support methods that don't accept PValues") {
    intercept[IllegalArgumentException](
      PtolemyValueFunction(pv.getMethod("slice", classOf[Option[Int]], classOf[Option[Int]]), List(pipl, pipl)))
  }

  test("ensure arg count and method arity match - int() with 1 extra argument") {
    intercept[IllegalArgumentException](PtolemyValueFunction(pv.getMethod("int"), List(pipl)))
  }

  test("ensure arg count and method arity match - slice(f,t) with only 1 argument") {
    intercept[IllegalArgumentException](
      PtolemyValueFunction(pv.getMethod("slice", classOf[PtolemyValue], classOf[PtolemyValue]), List(pipl)))
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
