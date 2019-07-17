package io.idml.functions

import io.idml.{IdmlContext, IdmlValue}
import io.idml.ast.IdmlFunction

/**
  * Base implementation of a function with no parameters
  */
abstract class IdmlFunction0 extends IdmlFunction {

  /**
    * The implementation of a variable-length function
    *
    * @param cursor The call site
    * @return The function return value
    */
  protected def apply(cursor: IdmlValue): IdmlValue

  /**
    * Invocation logic for handling variable-length functions
    *
    * @param ctx The execution context
    */
  override def invoke(ctx: IdmlContext): Unit = {
    ctx.enterFunc(this)
    ctx.cursor = apply(ctx.cursor)
    ctx.exitFunc(this)
  }

  val args = Nil
}
