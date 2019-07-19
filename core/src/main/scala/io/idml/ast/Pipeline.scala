package io.idml.ast

import io.idml.{IdmlContext, IdmlValue}

/** An executable pipeline of expressions */
case class Pipeline(exps: List[Expression]) extends Argument {

  // scalastyle:off method.name
  /** Helper to prepend a helper to a copy of this pipl */
  def ::(expr: Expression): Pipeline = copy(expr :: exps)

  /** Helper to join two pipls */
  def ::(prepend: Pipeline): Pipeline = copy(prepend.exps ::: exps)

  /** Helper to join two or more lists */
  def ::(prepend: Iterable[Node]): Pipeline = prepend.foldLeft(this) {
    case (p, expr: Expression) => expr :: p
    case (p, pipl: Pipeline)   => pipl :: p
  }
  // scalastyle:on method.name

  def validate() {
    require(exps.count(_.isInstanceOf[ExecNav]) == 1)
    require(exps.headOption.filter(_.isInstanceOf[ExecNav]).isDefined)
  }

  /** Extract the simple literal value of this node. This is a literal value with no transforms */
  def literal(): Option[IdmlValue] = {
    exps match {
      case ExecNavLiteral(Literal(value: IdmlValue)) :: Nil => Some(value)
      case _                                                => None
    }
  }

  /** A recursive function that applies the pathTracker visitor functions */
  def invoke(ctx: IdmlContext, exps: List[Expression]): Unit = exps match {
    case Nil => ()
    case exp :: tail =>
      ctx.enterPipl(this)

      exp.invoke(ctx)
      invoke(ctx, tail)

      ctx.exitPipl(this)
  }

  /** Run a pipl */
  def invoke(ctx: IdmlContext) {
    invoke(ctx, exps)
  }
}

object LiteralValue {
  def unapply(pipl: Pipeline): Option[IdmlValue] = pipl match {
    case Pipeline(List(ExecNavLiteral(Literal(value)))) => Some(value)
    case _                                              => None
  }
}
