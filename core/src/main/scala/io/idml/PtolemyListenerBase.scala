package io.idml

import io.idml.ast.{Assignment, Field, Maths, Pipeline, PtolemyFunction}

/**
  * The implements all the PtolemyListener methods with stubs. Extend at will
  */
class PtolemyListenerBase extends PtolemyListener {
  override def exitAssignment(ctx: PtolemyContext, assignment: Assignment): Unit = ()

  override def enterAssignment(ctx: PtolemyContext, assignment: Assignment): Unit = ()

  override def enterChain(ctx: PtolemyContext): Unit = ()

  override def exitChain(ctx: PtolemyContext): Unit = ()

  override def enterPath(context: PtolemyContext, path: Field): Unit = ()

  override def exitPath(context: PtolemyContext, path: Field): Unit = ()

  override def enterPipl(context: PtolemyContext, pipl: Pipeline): Unit = ()

  override def exitPipl(context: PtolemyContext, pipl: Pipeline): Unit = ()

  override def enterMaths(context: PtolemyContext, maths: Maths): Unit = ()

  override def exitMaths(context: PtolemyContext, maths: Maths): Unit = ()

  override def enterFunc(ctx: PtolemyContext, func: PtolemyFunction): Unit = ()

  override def exitFunc(ctx: PtolemyContext, func: PtolemyFunction): Unit = ()
}
