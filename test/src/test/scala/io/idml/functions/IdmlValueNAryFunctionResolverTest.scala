package io.idml.functions

import io.idml.datanodes.IString
import io.idml.IdmlValue
import io.idml.ast.{ExecNavLiteral, Literal, Pipeline}
import org.scalatest.FunSuite

class IdmlValueNAryFunctionResolverTest extends FunSuite {

  val pv = classOf[IdmlValue]

  val pipl = Pipeline(List(ExecNavLiteral(Literal(IString("")))))

  test("return none if a method can't be found") {
    assert(new IdmlValueNaryFunctionResolver().resolve("missing", Nil) === None)
  }

  test("return a function if a 1-arity function can be found") {
    assert(
      new IdmlValueNaryFunctionResolver()
        .resolve("format", List(pipl)) === Some(
        IdmlValueFunction(pv.getMethod("format", classOf[Seq[IdmlValue]]), List(pipl), isNAry = true)))
  }
}
