package io.idml.ast

import io.idml.datanodes.PObject
import io.idml.{Deleted, PtolemyContext, PtolemyNothing, PtolemyObject, PtolemyValue}

import scala.annotation.tailrec

case class Position(line: Int, character: Int)
case class Positions(start: Position, end: Position)
/** The assignment operator "=" */
case class Assignment(dest: List[String], exps: Pipeline, positions: Option[Positions] = None) extends Rule {

  /** Make an assignment */
  def invoke(ctx: PtolemyContext) {
    ctx.enterAssignment(this)

    exps.invoke(ctx)
    ctx.cursor match {
      case Deleted =>
        delete(ctx.output, dest)
      case reason: PtolemyNothing => ()
      case value: PObject if dest.isEmpty =>
        ctx.output.fields.clear()
        ctx.output.fields ++= value.deepCopy.fields
      case value: Any if dest.isEmpty => ()
      case value: Any =>
        assign(ctx.output, dest, value.deepCopy)
    }

    ctx.exitAssignment(this)
  }

  /** Traverse a JsonNode tree with a list of path parts with the ultimate goal of assigning a value */
  @tailrec
  final protected def assign(current: PtolemyObject, path: List[String], value: PtolemyValue) {
    path match {
      case Nil          => throw new IllegalArgumentException("Can't use an empty path")
      case head :: Nil  => assignValue(current, head, value)
      case head :: tail => assign(navigateToNext(current, head), tail, value)
    }
  }

  /** Adds a new field to an object. Does some additional, configurable checks */
  protected def assignValue(current: PtolemyObject, key: String, value: PtolemyValue) {
    current.fields(key) = value
  }

  /** Traverse a JsonNode tree with a list of path parts with the ultimate goal of deleting a value */
  @tailrec
  final protected def delete(current: PtolemyObject, path: List[String]) {
    path match {
      case Nil          => throw new IllegalArgumentException("Can't use an empty path")
      case head :: Nil  => deleteValue(current, head)
      case head :: tail => delete(navigateToNext(current, head), tail)
    }
  }

  protected def deleteValue(current: PtolemyObject, key: String) {
    current.fields.remove(key)
  }
}
