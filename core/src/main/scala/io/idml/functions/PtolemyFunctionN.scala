package io.idml.functions

import io.idml.{PtolemyContext, PtolemyValue}
import io.idml.ast.{Pipeline, PtolemyFunction}

/**
  * Base implementation of a function with a variable parameter list
  */
abstract class PtolemyFunctionN extends PtolemyFunction {

  /**
    * The ast nodes for the function arguments
    */
  val args: List[Pipeline]

  /**
    * The implementation of a variable-length function
    *
    * @param cursor The call site
    * @param args The fully-evaluated arguments
    * @return The function return value
    */
  protected def apply(cursor: PtolemyValue, args: Seq[PtolemyValue]): PtolemyValue

  /**
    * Invocation logic for handling variable-length functions
    *
    * @param ctx The execution context
    */
  override def invoke(ctx: PtolemyContext): Unit = {
    ctx.enterFunc(this)
    val results = args.map(_.eval(ctx))
    ctx.cursor = apply(ctx.cursor, results)
    ctx.exitFunc(this)
  }
}
