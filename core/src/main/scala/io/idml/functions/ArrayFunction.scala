package io.idml.functions

import io.idml.datanodes.PArray
import io.idml.ast.{Node, PtolemyFunction}
import io.idml.{InvalidCaller, PtolemyArray, PtolemyContext, PtolemyValue}

import scala.collection.{immutable, mutable}

/** Applies an expression to a series of nodes */
case class ArrayFunction(expr: Node) extends PtolemyFunction {

  def args: immutable.Nil.type = Nil

  def name: String = "array"

  /** Applies the expression to each item in the cursor */
  override def invoke(ctx: PtolemyContext): Unit = {
    // Preserve context
    val oldScope  = ctx.scope
    val oldOutput = ctx.output

    // Iterate items in the array
    ctx.cursor match {
      case array: PtolemyArray =>
        val results: mutable.Buffer[PtolemyValue] = array.items.map { item =>
          ctx.scope = item
          ctx.cursor = item
          expr.invoke(ctx)
          ctx.cursor
        }
        ctx.cursor = PArray(results)
      case _ =>
        ctx.cursor = InvalidCaller
    }

    ctx.scope = oldScope
    ctx.output = oldOutput
  }
}
