package io.idml

import io.idml.ast.{Assignment, Field, Maths, Pipeline, PtolemyFunction}

/** Hook into interpreter events */
abstract class PtolemyListener {
  def enterAssignment(ctx: PtolemyContext, assignment: Assignment): Unit

  def exitAssignment(ctx: PtolemyContext, assignment: Assignment): Unit

  def enterChain(ctx: PtolemyContext): Unit

  def exitChain(ctx: PtolemyContext): Unit

  def enterPath(context: PtolemyContext, path: Field): Unit

  def exitPath(context: PtolemyContext, path: Field): Unit

  def enterPipl(context: PtolemyContext, pipl: Pipeline): Unit

  def exitPipl(context: PtolemyContext, pipl: Pipeline): Unit

  def enterFunc(ctx: PtolemyContext, func: PtolemyFunction): Unit

  def exitFunc(ctx: PtolemyContext, func: PtolemyFunction): Unit

  def enterMaths(context: PtolemyContext, maths: Maths): Unit

  def exitMaths(context: PtolemyContext, maths: Maths): Unit

}
