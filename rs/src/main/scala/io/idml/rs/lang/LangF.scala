package io.idml.rs.lang

import cats._
import cats.implicits._
import io.idml.ast
import io.idml.ast.{And, Exists, GreaterThan, GreaterThanOrEqual, In, LessThan, LessThanOrEqual, Literal, Not, NotEquals, Or, Pipeline, Predicate, Substring, Underscore}

object LangF {

  sealed trait PredicateF[F]
  object PredicateF {
    case class AndF[F](left: F, right: F) extends PredicateF[F]
    case class OrF[F](left: F, right: F) extends PredicateF[F]
    case class Not[F](pred: F) extends PredicateF[F]
    case object Underscore extends PredicateF[Nothing]
    case class ExistsF[F](exps: Pipeline)
    case class SubstringF[F](left: Pipeline, right: Pipeline, cs: Boolean)
    case class EqualsF[F](left: Pipeline, right: Pipeline, cs: Boolean)
    case class NotEquals[F](left: Pipeline, right: Pipeline, cs: Boolean)
    case class InF[F](left: Pipeline, right: Pipeline, cs: Boolean)
    case class LessThanF[F](left: Pipeline, right: Pipeline, cs: Boolean)
    case class GreaterThanF[F](left: Pipeline, right: Pipeline, cs: Boolean)
    case class LessThanOrEqualF[F](left: Pipeline, right: Pipeline, cs: Boolean)
    case class GreaterThanOrEqualF[F](left: Pipeline, right: Pipeline, cs: Boolean)




    def embed(p: Predicate) = p match {
      case And(left, right) =>
      case Or(left, right) =>
      case Not(pred) =>
      case Underscore =>
      case Exists(exps) =>
      case Substring(left, right, cs) =>
      case ast.Equals(left, right, cs) =>
      case NotEquals(left, right, cs) =>
      case In(left, right, cs) =>
      case LessThan(left, right, cs) =>
      case GreaterThan(left, right, cs) =>
      case LessThanOrEqual(left, right, cs) =>
      case GreaterThanOrEqual(left, right, cs) =>
    }
  }


  sealed trait ExpressionF[F]
  object ExpressionF {
    // extra one for Pipeline
    case class PipelineF[F](exps: List[F])

    // normal ones
    case class IdmlFunction1F[F](arg: F)
    case class IdmlFunction[F]()
    case class IdmlFunction0[F]()
    case class CoalesceF[F](exps: List[F])
    case class FilterF(pred: Predicate)

    // navigation
    case class ExecNavLiteralF[F](lit: Literal)
    case class ExecNavVariableF[F]()
    case class ExecNavRelative[F]()
    case class ExecNavAbsolute[F]()
    case class ExecNavTempF[F]()
  }

}
