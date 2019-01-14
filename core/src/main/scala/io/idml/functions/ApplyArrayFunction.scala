package io.idml.functions

import io.idml.ast.PtolemyFunction
import io.idml.{NoIndex, PtolemyArray, PtolemyContext}

import scala.collection.immutable

/** Invoke a named mapping block within the document */
case class ApplyArrayFunction(n: String) extends PtolemyFunction {
  def name: String             = "applyArray"
  def args: immutable.Nil.type = Nil

  /** Execute the underlying block, if it exists */
  override def invoke(ctx: PtolemyContext) {

    // Preserve the existing scope and output object
    val oldScope  = ctx.scope
    val oldOutput = ctx.output

    val block = findBlock(ctx, n)

    ctx.enterFunc(this)

    ctx.cursor match {
      case array: PtolemyArray => applyBlock(ctx, block, array)
      case _                   => ctx.cursor = NoIndex
    }

    ctx.scope = oldScope
    ctx.output = oldOutput

    // FIXME: Note ambiguity of this event. Right now it's after the scope has been restored but could otherwise be before
    ctx.exitFunc(this)
  }
}
