package io.idml

import io.idml.datanodes.{IInt, IObject, IString}
import org.mockito.Answers
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar

import scala.collection.JavaConverters._

class IdmlChainTest extends AnyFunSuite with MockitoSugar with Matchers {

  test("Can't create an empty chain") {
    intercept[IllegalArgumentException](new IdmlChain())
  }

  test("Enter and exit chain methods are called") {
    val first    = mock[IdmlMapping]
    val ctx      = mock[IdmlContext]
    val listener = mock[IdmlListener]

    when(ctx.listeners).thenReturn(List(listener))

    val chain = new IdmlChain(first)
    chain.run(ctx)

    verify(ctx).enterChain()
    verify(ctx).exitChain()
  }

  test("Mappings have the run() method called") {
    val first    = mock[IdmlMapping]
    val second   = mock[IdmlMapping]
    val ctx      = mock[IdmlContext]
    val listener = mock[IdmlListener]

    when(ctx.listeners).thenReturn(List(listener))

    val chain = new IdmlChain(first, second)
    chain.run(ctx)

    verify(first).run(ctx)
    verify(second).run(ctx)
  }

  test("Mappings have the run() method called when invoked via the engine") {
    val first    = mock[IdmlMapping]
    val second   = mock[IdmlMapping]
    val ctx      = mock[IdmlContext]
    val listener = mock[IdmlListener]

    when(ctx.listeners).thenReturn(List(listener))

    val chain = new IdmlChain(first, second)
    val idml  = Idml.autoBuilder().build()
    idml.evaluate(chain, ctx)

    verify(first).run(ctx)
    verify(second).run(ctx)
  }

  test("chains run in order, operating on the same object") {
    val idml  = Idml.autoBuilder().build()
    val chain = idml.chain(List("a = initial + 1", "b = a + 1", "c = b + 1").map(idml.compile): _*)
    chain.run(IObject("initial" -> IInt(0))) must equal(
      IObject("a" -> IInt(1), "b" -> IInt(2), "c" -> IInt(3)))
    idml.evaluate(chain, IObject("initial" -> IInt(0))).getOutput must equal(
      IObject("a" -> IInt(1), "b" -> IInt(2), "c" -> IInt(3)))
  }

}
