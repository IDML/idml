package io.idml.ast

import io.idml.datanodes.{IArray, IObject, IString}
import io.idml.{IdmlContext, IdmlObject, IdmlValue}

import scala.collection.mutable

/** The top level exec node. The only requirement is that it can be invoked */
trait Node {
  def invoke(ctx: IdmlContext)

  /** Call invoke in a closed context. Useful for pipls  */
  def eval(ctx: IdmlContext): IdmlValue = {
    val tmp = ctx.cursor
    invoke(ctx)
    val res = ctx.cursor
    ctx.cursor = tmp
    res
  }

  /** Call invoke with a custom cursor. Useful inside preds */
  def eval(ctx: IdmlContext, cursor: IdmlValue): IdmlValue = {
    val tmp = ctx.cursor
    ctx.cursor = cursor
    invoke(ctx)
    val res = ctx.cursor
    ctx.cursor = tmp
    res
  }
}

/** The document. This contains all the executable blocks. Invoking it is an implicit call to the main block */
case class Document(blocks: Map[String, Block]) extends Node {
  val main = blocks.getOrElse(Document.Main, throw new IllegalArgumentException("missing main"))

  override def invoke(ctx: IdmlContext) {
    main.invoke(ctx)
  }
}

// used to tag function arguments
trait Argument extends Node {}

object Document {
  val Main  = "main"
  val empty = Document(Map(Main -> Block(Main, Nil)))
}

/** A reusable code block */
case class Block(name: String, rules: List[Rule]) extends Node {
  def invoke(ctx: IdmlContext) {
    rules foreach (_.invoke(ctx))
  }
}

/** A single executable line */
trait Rule extends Node {

  /** Navigate to the next node in the tree  */
  protected def navigateToNext(current: IdmlObject, key: String): IdmlObject =
    current.fields.get(key) match {
      case None =>
        // This object doesn't exist, let's create it
        val next = IObject()
        current.fields(key) = next
        next
      case Some(obj: IObject) =>
        // We've been here before, return the object
        obj
      case Some(other) =>
        // Question: What's the desired behaviour when we're given a = 1 and then a.b = 1; right now we replace the
        // primitive with an object, effectively throwing away the value but we could just as easily reintroduce it as
        // if the rule was rewritten to a.value = 1 or reject the mapping entirely.
        val next = IObject()
        current.fields(key) = next
        next
    }
}

/** A component in an executable rule */
trait Expression extends Node

/** Execute a literal expression */
case class Literal(value: IdmlValue) extends Node {
  def invoke(ctx: IdmlContext) {
    ctx.cursor = value
  }
}

/** Navigate to an object field */
case class Field(name: String) extends Expression {
  def invoke(ctx: IdmlContext) {
    ctx.enterPath(this)
    ctx.cursor = ctx.cursor.get(name)
    ctx.exitPath(this)
  }
}

/** Expressions like **.x mean "find 'x' at any depth" */
case object AnyExpr extends Expression {
  def invoke(ctx: IdmlContext) {
    ???
  }
}

/** Expressions like [n, m] slice an array */
case class Slice(from: Option[Int], to: Option[Int]) extends Expression {
  def invoke(ctx: IdmlContext) {
    ctx.cursor = ctx.cursor.slice(from, to)
  }
}

/** Expressions like [n] retrieve a single item from an array */
case class Index(index: Int) extends Expression {
  def invoke(ctx: IdmlContext) {
    ctx.cursor = ctx.cursor.get(index)
  }
}

case class AstArray(pipls: List[Node]) extends Expression {
  override def invoke(ctx: IdmlContext): Unit = {
    ctx.cursor = IArray(pipls.map(_.eval(ctx)).toBuffer)
  }
}

case class AstObject(piplmap: Map[IString, Pipeline]) extends Expression {
  override def invoke(ctx: IdmlContext): Unit = {
    ctx.cursor = IObject(piplmap.map { case (k, v) => (k.value, v.eval(ctx)) }.toList: _*)
  }
}

/** Execute a mathematical expression */
case class Maths(left: Node, operator: String, right: Node) extends Expression {
  def invoke(ctx: IdmlContext): Unit = {
    ctx.enterMaths(this)
    operator match {
      case "/" => ctx.cursor = left.eval(ctx) / right.eval(ctx)
      case "*" => ctx.cursor = left.eval(ctx) * right.eval(ctx)
      case "+" => ctx.cursor = left.eval(ctx) + right.eval(ctx)
      case "-" => ctx.cursor = left.eval(ctx) - right.eval(ctx)
      case _   => throw new IllegalStateException()
    }
    ctx.exitMaths(this)
  }
}
