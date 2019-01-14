package io.idml.functions

import io.idml.datanodes.PString
import io.idml.PtolemyValue
import io.idml.ast.{ExecNavLiteral, Literal, Pipeline}
import org.scalatest.FunSuite

class PtolemyValueNAryFunctionResolverTest extends FunSuite {

  val pv = classOf[PtolemyValue]

  val pipl = Pipeline(List(ExecNavLiteral(Literal(PString("")))))

  test("return none if a method can't be found") {
    assert(new PtolemyValueNaryFunctionResolver().resolve("missing", Nil) === None)
  }

  test("return a function if a 1-arity function can be found") {
    assert(
      new PtolemyValueNaryFunctionResolver()
        .resolve("format", List(pipl)) === Some(
        PtolemyValueFunction(pv.getMethod("format", classOf[Seq[PtolemyValue]]), List(pipl), isNAry = true)))
  }
}
