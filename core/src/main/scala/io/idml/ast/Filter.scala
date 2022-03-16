package io.idml.ast

import io.idml.datanodes.IArray
import io.idml.{Filtered, IdmlArray, IdmlContext, IdmlNothing, IdmlString, IdmlValue}

/** Top level class for implementing predicates */
trait Predicate extends Argument {
  def invoke(ctx: IdmlContext) {
    val tmp = ctx.cursor
    if (!predicate(ctx, tmp)) {
      ctx.cursor = Filtered
    }
  }

  /** Let's create a simple way of making predicate evaluation */
  def predicate(ctx: IdmlContext, cursor: IdmlValue): Boolean
}

/** Top level class for implementing filters */
case class Filter(pred: Predicate) extends Expression {

  protected def invokeOnArray(ctx: IdmlContext): Unit = {
    val array = ctx.cursor.asInstanceOf[IdmlArray]
    val results = array.items.filter { item =>
      ctx.scope = item
      pred.predicate(ctx, item)
    }

    if (results.isEmpty) {
      ctx.cursor = Filtered
    } else {
      ctx.cursor = IArray(results)
    }
  }

  protected def invokeOnScalar(ctx: IdmlContext): Unit = {
    ctx.scope = ctx.cursor
    pred.invoke(ctx)
  }

  def invoke(ctx: IdmlContext) {
    val tmp = ctx.scope
    ctx.cursor match {
      case arr: IdmlArray => invokeOnArray(ctx)
      case _              => invokeOnScalar(ctx)
    }
    ctx.scope = tmp
  }
}

/** Apply logical conjunction */
case class And(left: Predicate, right: Predicate) extends Predicate {
  def predicate(ctx: IdmlContext, cursor: IdmlValue): Boolean =
    left.predicate(ctx, cursor) && right.predicate(ctx, cursor)
}

/** Apply logical disjunction */
case class Or(left: Predicate, right: Predicate) extends Predicate {
  def predicate(ctx: IdmlContext, cursor: IdmlValue): Boolean =
    left.predicate(ctx, cursor) || right.predicate(ctx, cursor)
}

/** Apply negation */
case class Not(pred: Predicate) extends Predicate {
  def predicate(ctx: IdmlContext, cursor: IdmlValue): Boolean =
    !pred.predicate(ctx, cursor)
}

case object UnderscorePred extends Predicate {
  def predicate(ctx: IdmlContext, cursor: IdmlValue): Boolean = true
}

/** Check to see if a node exists */
case class Exists(exps: Pipeline) extends Predicate {
  def predicate(ctx: IdmlContext, cursor: IdmlValue): Boolean =
    !exps.eval(ctx, cursor).isInstanceOf[IdmlNothing]
}

/** Check to see if a substring exists */
case class Substring(left: Pipeline, right: Pipeline, cs: Boolean) extends Predicate {

  protected def str(value: IdmlValue): String = value match {
    case s: IdmlString => s.value
    case _             => ""
  }

  def predicate(ctx: IdmlContext, cursor: IdmlValue): Boolean = {
    val lvalue = str(left.eval(ctx, cursor))
    val rvalue = str(right.eval(ctx, cursor))
    lvalue != "" && rvalue != "" && lvalue.contains(rvalue)
  }
}

/** Check for equality */
case class Equals(left: Pipeline, right: Pipeline, cs: Boolean) extends Predicate {
  def predicate(ctx: IdmlContext, cursor: IdmlValue): Boolean = {
    val lvalue = left.eval(ctx)
    val rvalue = right.eval(ctx)
    lvalue == rvalue && !lvalue.isNothing.value
  }
}

/** Check for inequality */
case class NotEquals(left: Pipeline, right: Pipeline, cs: Boolean) extends Predicate {
  def predicate(ctx: IdmlContext, cursor: IdmlValue): Boolean = {
    val lvalue = left.eval(ctx)
    val rvalue = right.eval(ctx)
    lvalue.isNothing || rvalue.isNothing || lvalue != rvalue
  }
}

/** The pickle "in" operator */
case class In(left: Pipeline, right: Pipeline, cs: Boolean) extends Predicate {
  def predicate(ctx: IdmlContext, cursor: IdmlValue): Boolean = {
    (left.eval(ctx), right.eval(ctx)) match {
      case (l: IdmlValue, r: IdmlArray) =>
        r.items.contains(l)
      case (l: IdmlString, r: IdmlString) =>
        r.value.split(',').contains(l.value)
      case _ => false
    }
  }
}

class Contains(left: Pipeline, right: Pipeline, cs: Boolean) extends In(right, left, cs)

case class LessThan(left: Pipeline, right: Pipeline, cs: Boolean) extends Predicate {
  def predicate(ctx: IdmlContext, cursor: IdmlValue): Boolean = {
    Ordering[IdmlValue].lt(left.eval(ctx), right.eval(ctx))
  }
}

case class GreaterThan(left: Pipeline, right: Pipeline, cs: Boolean) extends Predicate {
  def predicate(ctx: IdmlContext, cursor: IdmlValue): Boolean = {
    Ordering[IdmlValue].gt(left.eval(ctx), right.eval(ctx))
  }
}

case class LessThanOrEqual(left: Pipeline, right: Pipeline, cs: Boolean) extends Predicate {
  def predicate(ctx: IdmlContext, cursor: IdmlValue): Boolean = {
    Ordering[IdmlValue].lteq(left.eval(ctx), right.eval(ctx))
  }
}

case class GreaterThanOrEqual(left: Pipeline, right: Pipeline, cs: Boolean) extends Predicate {
  def predicate(ctx: IdmlContext, cursor: IdmlValue): Boolean = {
    Ordering[IdmlValue].gteq(left.eval(ctx), right.eval(ctx))
  }
}
