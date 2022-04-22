package io.idml.functions

import io.idml.datanodes.IArray
import io.idml.ast.{IdmlFunction, Node}
import io.idml.{IdmlArray, IdmlContext, IdmlValue, InvalidCaller}

import scala.collection.{immutable, mutable}

/** Applies an expression to a series of nodes */
case class ArrayFunction(expr: Node) extends IdmlFunction {

  def args: immutable.Nil.type = Nil

  def name: String = "array"

  /** Applies the expression to each item in the cursor */
  override def invoke(ctx: IdmlContext): Unit = {
    // Preserve context
    val oldScope  = ctx.scope
    val oldOutput = ctx.output

    // Iterate items in the array
    ctx.cursor match {
      case array: IdmlArray =>
        val results: mutable.Buffer[IdmlValue] = array.items.map { item =>
          ctx.scope = item
          ctx.cursor = item
          expr.invoke(ctx)
          ctx.cursor
        }
        ctx.cursor = IArray(results)
      case _                =>
        ctx.cursor = InvalidCaller
    }

    ctx.scope = oldScope
    ctx.output = oldOutput
  }
}
