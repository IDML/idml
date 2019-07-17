package io.idml

import io.idml.ast.{Assignment, Field, IdmlFunction, Maths, Pipeline}

/** Hook into interpreter events */
abstract class IdmlListener {
  def enterAssignment(ctx: IdmlContext, assignment: Assignment): Unit

  def exitAssignment(ctx: IdmlContext, assignment: Assignment): Unit

  def enterChain(ctx: IdmlContext): Unit

  def exitChain(ctx: IdmlContext): Unit

  def enterPath(context: IdmlContext, path: Field): Unit

  def exitPath(context: IdmlContext, path: Field): Unit

  def enterPipl(context: IdmlContext, pipl: Pipeline): Unit

  def exitPipl(context: IdmlContext, pipl: Pipeline): Unit

  def enterFunc(ctx: IdmlContext, func: IdmlFunction): Unit

  def exitFunc(ctx: IdmlContext, func: IdmlFunction): Unit

  def enterMaths(context: IdmlContext, maths: Maths): Unit

  def exitMaths(context: IdmlContext, maths: Maths): Unit

}
