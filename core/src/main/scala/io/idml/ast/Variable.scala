package io.idml.ast

import io.idml.datanodes.PObject
import io.idml._

import scala.annotation.tailrec

case object Variable {
  final val stateKey = "variable_storage"
}

/** The variable assignment operator "let $a = $b" */
case class Variable(dest: List[String], exps: Pipeline) extends Rule {
  require(dest.nonEmpty, "Cannot make an assignment to an empty path")

  /** Make a variable assignment */
  def invoke(ctx: IdmlContext) {
    val variableStorage = ctx.state.getOrElseUpdate(Variable.stateKey, PObject()).asInstanceOf[IdmlObject]

    exps.invoke(ctx)
    ctx.cursor match {
      case Deleted =>
        delete(variableStorage, dest)
      case reason: IdmlNothing => ()
      case value: Any =>
        assign(variableStorage, dest, value.deepCopy)
    }
  }

  /** Traverse a JsonNode tree with a list of path parts with the ultimate goal of assigning a value */
  @tailrec
  final protected def assign(current: IdmlObject, path: List[String], value: IdmlValue) {
    path match {
      case Nil          => throw new IllegalArgumentException("Can't use an empty path")
      case head :: Nil  => assignValue(current, head, value)
      case head :: tail => assign(navigateToNext(current, head), tail, value)
    }
  }

  /** Adds a new field to an object. Does some additional, configurable checks */
  protected def assignValue(current: IdmlObject, key: String, value: IdmlValue) {
    current.fields(key) = value
  }

  /** Traverse a JsonNode tree with a list of path parts with the ultimate goal of deleting a value */
  @tailrec
  final protected def delete(current: IdmlObject, path: List[String]) {
    path match {
      case Nil          => throw new IllegalArgumentException("Can't use an empty path")
      case head :: Nil  => deleteValue(current, head)
      case head :: tail => delete(navigateToNext(current, head), tail)
    }
  }

  protected def deleteValue(current: IdmlObject, key: String) {
    current.fields.remove(key)
  }
}
