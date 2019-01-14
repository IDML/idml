package io.idml.functions

import io.idml.{PtolemyContext, PtolemyValue}
import io.idml.ast.PtolemyFunction

/**
  * Base implementation of a function with no parameters
  */
abstract class PtolemyFunction0 extends PtolemyFunction {

  /**
    * The implementation of a variable-length function
    *
    * @param cursor The call site
    * @return The function return value
    */
  protected def apply(cursor: PtolemyValue): PtolemyValue

  /**
    * Invocation logic for handling variable-length functions
    *
    * @param ctx The execution context
    */
  override def invoke(ctx: PtolemyContext): Unit = {
    ctx.enterFunc(this)
    ctx.cursor = apply(ctx.cursor)
    ctx.exitFunc(this)
  }

  val args = Nil
}
