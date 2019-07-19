package io.idml.functions

import io.idml.{IdmlContext, IdmlValue}
import io.idml.ast.{IdmlFunction, Pipeline}

/**
  * Base implementation of a function with 3 parameters
  */
abstract class IdmlFunction1 extends IdmlFunction {

  /**
    * The ast node for the parameter
    */
  val arg: Pipeline

  /**
    * The implementation of a variable-length function
    *
    * @param cursor The call site
    * @param val1 The fully-evaluated first parameter
    * @return The function return value
    */
  protected def apply(cursor: IdmlValue, val1: IdmlValue): IdmlValue

  /**
    * Invocation logic for handling variable-length functions
    *
    * @param ctx The execution context
    */
  override def invoke(ctx: IdmlContext): Unit = {
    ctx.enterFunc(this)
    val val1 = arg.eval(ctx)
    ctx.cursor = apply(ctx.cursor, val1)
    ctx.exitFunc(this)
  }

  val args = List(arg)
}
