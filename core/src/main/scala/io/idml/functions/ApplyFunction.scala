package io.idml.functions

import io.idml.ast.IdmlFunction
import io.idml.{IdmlArray, IdmlContext, IdmlNothing}

import scala.collection.immutable

/** Invoke a named mapping block within the document */
case class ApplyFunction(n: String) extends IdmlFunction {
  def name: String             = "apply"
  def args: immutable.Nil.type = Nil

  /** Execute the underlying block, if it exists */
  override def invoke(ctx: IdmlContext) {
    // Preserve the existing scope and output object
    val oldScope  = ctx.scope
    val oldOutput = ctx.output

    val block = findBlock(ctx, n)

    ctx.enterFunc(this)

    ctx.cursor match {
      case _: IdmlNothing   => ()
      case array: IdmlArray => applyBlockToArray(ctx, block, array)
      case _                => applyBlock(ctx, block, ctx.cursor)
    }

    ctx.scope = oldScope
    ctx.output = oldOutput

    ctx.exitFunc(this)
  }
}
