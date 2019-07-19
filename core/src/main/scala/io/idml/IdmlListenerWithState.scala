package io.idml

/** Helper that adds state */
abstract class IdmlListenerWithState[A] extends IdmlListenerBase {

  /** Create a new starting state object */
  protected def defaultState(ctx: IdmlContext): A

  /** Return the listener state */
  def state(ctx: IdmlContext): A = {
    ctx.state.getOrElseUpdate(this.getClass, defaultState(ctx)).asInstanceOf[A]
  }
}
