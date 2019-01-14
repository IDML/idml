package io.idml.ast

import io.idml.datanodes.PObject
import io.idml.{PtolemyContext, PtolemyNothing, PtolemyObject, PtolemyValue}

import scala.annotation.tailrec

/** The reassignment operator ":" */
case class Reassignment(dest: List[String], exps: Pipeline) extends Rule {
  require(dest.nonEmpty, "Cannot make a reassignment to an empty path")

  /** Make an assignment */
  def invoke(ctx: PtolemyContext) {
    val (parent, lastKey, lastValue) = ctx.output match {
      case obj: PObject => findDestination(obj, dest)
      case _ =>
        throw new IllegalStateException("Reassignment needs an object scope")
    }

    ctx.cursor = lastValue
    exps.invoke(ctx)

    ctx.cursor match {
      case reason: PtolemyNothing =>
        deleteEmptyPaths(ctx.output, dest)
      case value: Any =>
        parent.fields(lastKey) = ctx.cursor
    }
  }

  @tailrec
  final protected def findDestination(parent: PtolemyObject, path: List[String]): (PtolemyObject, String, PtolemyValue) = path match {
    case Nil            => throw new IllegalArgumentException("Empty paths not supported")
    case lastKey :: Nil => (parent, lastKey, parent.get(lastKey))
    case next :: tail   => findDestination(navigateToNext(parent, next), tail)
  }

  /**
    * Check all the paths bottom up, delete them if they are empty
    * @param current top level pobject to navigate from
    * @param path path to navigate down
    */
  protected def deleteEmptyPaths(current: PtolemyObject, path: List[String]): Unit = path match {
    case Nil =>
      ()
    case head :: tail =>
      val next = current.get(head)
      next match {
        case p: PtolemyObject =>
          deleteEmptyPaths(p, tail)
          if (p.isEmpty.value) {
            current.remove(head)
          }
        case _ =>
          ()
      }
  }
}
