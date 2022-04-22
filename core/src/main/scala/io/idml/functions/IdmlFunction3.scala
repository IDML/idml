package io.idml.functions

import io.idml.{IdmlContext, IdmlValue}
import io.idml.ast.{IdmlFunction, Pipeline}

/** Base implementation of a function with 3 parameters
  */
abstract class IdmlFunction3 extends IdmlFunction {

  /** The ast node for the first parameters
    */
  val arg1: Pipeline

  /** The ast node for the second parameters
    */
  val arg2: Pipeline

  /** The ast node for the third parameters
    */
  val arg3: Pipeline

  val args = List(arg1, arg2, arg3)

  /** The implementation of a variable-length function
    *
    * @param cursor
    *   The call site
    * @param val1
    *   The fully-evaluated first parameter
    * @param val2
    *   The fully-evaluated second parameter
    * @param val3
    *   The fully-evaluated parameter parameter
    * @return
    *   The function return value
    */
  protected def apply(
      cursor: IdmlValue,
      val1: IdmlValue,
      val2: IdmlValue,
      val3: IdmlValue): IdmlValue

  /** Invocation logic for handling variable-length functions
    *
    * @param ctx
    *   The execution context
    */
  override def invoke(ctx: IdmlContext): Unit = {
    ctx.enterFunc(this)
    val val1 = arg1.eval(ctx)
    val val2 = arg2.eval(ctx)
    val val3 = arg3.eval(ctx)
    ctx.cursor = apply(ctx.cursor, val1, val2, val3)
    ctx.exitFunc(this)
  }
}
