package io.idml.functions

import io.idml.datanodes.{IArray, IObject}
import io.idml._
import io.idml.ast.{IdmlFunction, Node}

import scala.collection.{immutable, mutable}

case class GroupsByFunction(expr: Node) extends IdmlFunction {
  def args: immutable.Nil.type = Nil

  def name: String = "groupBySafe"

  protected def extractOpt(ctx: IdmlContext, item: IdmlValue): Option[IdmlValue] = {
    ctx.scope = item
    ctx.cursor = item
    expr.invoke(ctx)
    if (ctx.cursor.isInstanceOf[IdmlNothing]) {
      None
    } else {
      Some(ctx.cursor)
    }
  }

  override def invoke(ctx: IdmlContext): Unit = {
    // Preserve context
    val oldScope  = ctx.scope
    val oldOutput = ctx.output

    // Iterate items in the array
    ctx.cursor match {
      case nothing: IdmlNothing =>
        nothing
      case array: IdmlArray =>
        val results = {
          val vs = array.items
            .flatMap(x =>
              extractOpt(ctx, x).map { v =>
                v -> x
            })
            .groupBy(_._1)
            .toList
            .sortBy(_._1)
            .map { case (k, v) => IObject("key" -> k, "values" -> IArray(v.map(_._2))) }
          IArray(vs.toBuffer[IdmlValue])
        }
        ctx.cursor = results
      case _ =>
        ctx.cursor = InvalidCaller
    }

    ctx.scope = oldScope
    ctx.output = oldOutput
  }
}
