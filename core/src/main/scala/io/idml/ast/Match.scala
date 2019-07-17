package io.idml.ast

import io.idml.{EmptyCoalesce, IdmlContext, IdmlNothing, IdmlValue}

case class Match(input: Pipeline, cases: List[Case]) extends Expression {
  override def invoke(ctx: IdmlContext): Unit = {
    val tmp = ctx.scope
    ctx.scope = input.eval(ctx)
    ctx.cursor = cases.view
      .find(_.matches(ctx))
      .map(_.eval(ctx))
      .getOrElse(EmptyCoalesce)
    ctx.scope = tmp
  }
}

case class Case(value: Predicate, result: Pipeline) extends Expression {
  def matches(ctx: IdmlContext): Boolean = value.predicate(ctx, ctx.cursor)

  override def invoke(ctx: IdmlContext): Unit = {
    if (matches(ctx)) {
      ctx.cursor = result.eval(ctx)
    } else {
      ctx.cursor = EmptyCoalesce
    }
  }
}

case class If(pred: Predicate, `then`: Pipeline, `else`: Option[Pipeline]) extends Expression {
  override def invoke(ctx: IdmlContext): Unit = {
    ctx.cursor = (pred.predicate(ctx, ctx.cursor), `else`) match {
      case (true, _)            => `then`.eval(ctx)
      case (false, Some(other)) => other.eval(ctx)
      case _                    => EmptyCoalesce
    }
  }
}
