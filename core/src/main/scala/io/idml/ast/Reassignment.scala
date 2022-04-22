package io.idml.ast

import io.idml.datanodes.IObject
import io.idml.{IdmlContext, IdmlNothing, IdmlObject, IdmlValue}

import scala.annotation.tailrec

/** The reassignment operator ":" */
case class Reassignment(dest: List[String], exps: Pipeline) extends Rule {
  require(dest.nonEmpty, "Cannot make a reassignment to an empty path")

  /** Make an assignment */
  def invoke(ctx: IdmlContext) {
    val (parent, lastKey, lastValue) = ctx.output match {
      case obj: IObject => findDestination(obj, dest)
      case _            =>
        throw new IllegalStateException("Reassignment needs an object scope")
    }

    ctx.cursor = lastValue
    exps.invoke(ctx)

    ctx.cursor match {
      case reason: IdmlNothing =>
        deleteEmptyPaths(ctx.output, dest)
      case value: Any          =>
        parent.fields(lastKey) = ctx.cursor
    }
  }

  @tailrec
  final protected def findDestination(
      parent: IdmlObject,
      path: List[String]): (IdmlObject, String, IdmlValue) =
    path match {
      case Nil            => throw new IllegalArgumentException("Empty paths not supported")
      case lastKey :: Nil => (parent, lastKey, parent.get(lastKey))
      case next :: tail   => findDestination(navigateToNext(parent, next), tail)
    }

  /** Check all the paths bottom up, delete them if they are empty
    * @param current
    *   top level pobject to navigate from
    * @param path
    *   path to navigate down
    */
  protected def deleteEmptyPaths(current: IdmlObject, path: List[String]): Unit =
    path match {
      case Nil          =>
        ()
      case head :: tail =>
        val next = current.get(head)
        next match {
          case p: IdmlObject =>
            deleteEmptyPaths(p, tail)
            if (p.isEmpty.value) {
              current.remove(head)
            }
          case _             =>
            ()
        }
    }
}
