package io.idml.ast

import io.idml.{EmptyCoalesce, PtolemyContext, PtolemyNothing}

/** Try to execute a series of pipls and pick the first with a value */
case class Coalesce(exps: List[Pipeline]) extends Expression {
  def invoke(ctx: PtolemyContext) {
    val tmp = ctx.scope
    ctx.scope = ctx.cursor
    ctx.cursor = exps.view
      .map(_.eval(ctx))
      .find(!_.isInstanceOf[PtolemyNothing])
      .getOrElse(EmptyCoalesce)
    ctx.scope = tmp
  }
}
