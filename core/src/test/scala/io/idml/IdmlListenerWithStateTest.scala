package io.idml

import java.util.concurrent.atomic.AtomicInteger
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.mockito.MockitoSugar

class IdmlListenerWithStateTest extends AnyFunSuite with MockitoSugar {

  /** A test implementation. Typically this would be a more complex object than an integer */
  class TestImpl extends IdmlListenerWithState[AtomicInteger] {
    override protected def defaultState(ctx: IdmlContext) =
      new AtomicInteger(1)
  }

  test("The defaultState function provides a starting value") {
    val ctx      = new IdmlContext()
    val listener = new TestImpl()
    assert(listener.state(ctx).get() === 1)
  }

  test("The state can be returned and updated") {
    val ctx      = new IdmlContext()
    val listener = new TestImpl()
    listener.state(ctx).set(2)
    assert(listener.state(ctx).get() === 2)
  }
}
