package io.idml

/** Helper that adds state */
abstract class PtolemyListenerWithState[A] extends PtolemyListenerBase {

  /** Create a new starting state object */
  protected def defaultState(ctx: PtolemyContext): A

  /** Return the listener state */
  def state(ctx: PtolemyContext): A = {
    ctx.state.getOrElseUpdate(this.getClass, defaultState(ctx)).asInstanceOf[A]
  }
}
