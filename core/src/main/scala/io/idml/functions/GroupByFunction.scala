package io.idml.functions

import io.idml.datanodes.{PArray, PObject}
import io.idml._
import io.idml.ast.{Node, PtolemyFunction}

import scala.collection.{immutable, mutable}

case class GroupByFunction(expr: Node) extends PtolemyFunction {
  def args: immutable.Nil.type = Nil

  def name: String = "groupBy"

  protected def extractOpt(ctx: PtolemyContext, item: PtolemyValue): Option[PtolemyValue] = {
    ctx.scope = item
    ctx.cursor = item
    expr.invoke(ctx)
    if (ctx.cursor.isInstanceOf[PtolemyNothing]) {
      None
    } else {
      Some(ctx.cursor)
    }
  }

  override def invoke(ctx: PtolemyContext): Unit = {
    // Preserve context
    val oldScope  = ctx.scope
    val oldOutput = ctx.output

    // Iterate items in the array
    ctx.cursor match {
      case nothing: PtolemyNothing =>
        nothing
      case array: PtolemyArray =>
        val results = {
          val vs = array.items
            .flatMap(x =>
              extractOpt(ctx, x).map { v =>
                v.toStringValue -> x
            })
            .groupBy(_._1)
            .mapValues(_.map(_._2))
            .mapValues(PArray(_))
          PObject(mutable.SortedMap[String, PtolemyValue](vs.toList: _*))
        }
        ctx.cursor = results
      case _ =>
        ctx.cursor = InvalidCaller
    }

    ctx.scope = oldScope
    ctx.output = oldOutput
  }
}
