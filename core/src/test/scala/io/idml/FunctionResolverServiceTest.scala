package io.idml

import io.idml.datanodes.IString
import io.idml.ast._
import io.idml.functions.{ApplyFunction, ArrayFunction, IdmlValueFunction}
import org.scalatest.FunSuite

class FunctionResolverServiceTest extends FunSuite {

  // FIXME this how we programmatically generate the parameter list in f("main") we need an easier way of doing this!
  val blockNameLiteral      = Pipeline(List(ExecNavLiteral(Literal(IString("main")))))
  val pathExpression        = Pipeline(List(ExecNavRelative, Field("p")))
  val twoArgumentExpression = List(blockNameLiteral, blockNameLiteral)
  val threeArgumentExpression =
    List(blockNameLiteral, blockNameLiteral, blockNameLiteral)

  test("support the apply method") {
    assert(
      new FunctionResolverService()
        .resolve("apply", List(blockNameLiteral)) === ApplyFunction("main"))
  }

  test("support the array method with a block") {
    pendingUntilFixed {
      assert(
        new FunctionResolverService()
          .resolve("array", List(blockNameLiteral)) === ArrayFunction(ApplyFunction("main")))
    }
  }

  test("support the array method with an expression") {
    assert(
      new FunctionResolverService()
        .resolve("array", List(pathExpression)) === ArrayFunction(pathExpression))
  }

  test("resolves a 0-arity IdmlValue function") {
    assert(
      new FunctionResolverService()
        .resolve("int", Nil) === IdmlValueFunction(classOf[IdmlValue].getMethod("int"), Nil))
  }

  test("resolves a 2-arity IdmlValue function") {
    assert(
      new FunctionResolverService().resolve("slice", twoArgumentExpression) ===
        IdmlValueFunction(classOf[IdmlValue]
                            .getMethod("slice", classOf[IdmlValue], classOf[IdmlValue]),
                          twoArgumentExpression))
  }

  test("fails if a function is missing") {
    intercept[UnknownFunctionException](new FunctionResolverService().resolve("missing", Nil))
    intercept[UnknownFunctionException](new FunctionResolverService().resolve("missing", twoArgumentExpression))
    intercept[UnknownFunctionException](new FunctionResolverService().resolve("slice", threeArgumentExpression))
  }

}
