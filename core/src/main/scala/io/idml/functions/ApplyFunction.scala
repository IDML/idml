package io.idml.functions

import io.idml.ast.PtolemyFunction
import io.idml.{PtolemyArray, PtolemyContext, PtolemyNothing}

import scala.collection.immutable

/** Invoke a named mapping block within the document */
case class ApplyFunction(n: String) extends PtolemyFunction {
  def name: String             = "apply"
  def args: immutable.Nil.type = Nil

  /** Execute the underlying block, if it exists */
  override def invoke(ctx: PtolemyContext) {
    // Preserve the existing scope and output object
    val oldScope  = ctx.scope
    val oldOutput = ctx.output

    val block = findBlock(ctx, n)

    ctx.enterFunc(this)

    ctx.cursor match {
      case _: PtolemyNothing   => ()
      case array: PtolemyArray => applyBlockToArray(ctx, block, array)
      case _                   => applyBlock(ctx, block, ctx.cursor)
    }

    ctx.scope = oldScope
    ctx.output = oldOutput

    ctx.exitFunc(this)
  }
}
