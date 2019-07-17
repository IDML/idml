package io.idml

import io.idml.ast.{Assignment, Field, IdmlFunction, Maths, Pipeline}

/**
  * The implements all the IdmlListener methods with stubs. Extend at will
  */
class IdmlListenerBase extends IdmlListener {
  override def exitAssignment(ctx: IdmlContext, assignment: Assignment): Unit = ()

  override def enterAssignment(ctx: IdmlContext, assignment: Assignment): Unit = ()

  override def enterChain(ctx: IdmlContext): Unit = ()

  override def exitChain(ctx: IdmlContext): Unit = ()

  override def enterPath(context: IdmlContext, path: Field): Unit = ()

  override def exitPath(context: IdmlContext, path: Field): Unit = ()

  override def enterPipl(context: IdmlContext, pipl: Pipeline): Unit = ()

  override def exitPipl(context: IdmlContext, pipl: Pipeline): Unit = ()

  override def enterMaths(context: IdmlContext, maths: Maths): Unit = ()

  override def exitMaths(context: IdmlContext, maths: Maths): Unit = ()

  override def enterFunc(ctx: IdmlContext, func: IdmlFunction): Unit = ()

  override def exitFunc(ctx: IdmlContext, func: IdmlFunction): Unit = ()
}
