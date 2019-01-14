package io.idml

import io.idml.datanodes.PObject
import io.idml.ast.{Assignment, Document, Field, Maths, Pipeline, PtolemyFunction}
import scala.collection.mutable

/** The interpreter's state object */
class PtolemyContext( /** The mappings document */
                     var doc: Document,
                     /** The root of the input object */
                     var input: PtolemyValue,
                     /** The root of the output object */
                     var output: PtolemyObject,
                     /** The list of listeners that hook in to events around the system */
                     var listeners: List[PtolemyListener],
                     /** A bag of state that can be used by listeners */
                     val state: mutable.Map[Any, Any]) {

  def this(input: PtolemyValue, output: PtolemyObject, listeners: List[PtolemyListener]) {
    this(Document.empty, input, output, listeners, mutable.Map())
  }

  def this(mapping: Document, input: PtolemyValue, output: PtolemyObject) {
    this(mapping, input, output, Nil, mutable.Map())
  }

  def this(input: PtolemyValue, output: PtolemyObject) {
    this(input, output, Nil)
  }

  def this(input: PtolemyValue) {
    this(input, PObject(), Nil)
  }

  def this() {
    this(PtolemyNull, PObject(), Nil)
  }

  /** The current right-hand side value as we traverse the input. Effectively "this" for the node methods */
  var cursor: PtolemyValue = input

  /** The pointer to the right-hand side. This changes depending on whether we're assigning, reassigning and whether
    * we are inside a call to apply() */
  var scope: PtolemyValue = input

  def enterAssignment(assign: Assignment): Unit = {
    listeners.foreach(_.enterAssignment(this, assign))
  }

  def exitAssignment(assign: Assignment): Unit = {
    listeners.foreach(_.exitAssignment(this, assign))
  }

  def enterChain(): Unit = {
    listeners.foreach(_.enterChain(this))
  }

  def exitChain(): Unit = {
    listeners.foreach(_.exitChain(this))
  }

  def enterPath(path: Field): Unit = {
    listeners.foreach(_.enterPath(this, path))
  }

  def exitPath(path: Field): Unit = {
    listeners.foreach(_.exitPath(this, path))
  }

  def enterPipl(pipl: Pipeline): Unit = {
    listeners.foreach(_.enterPipl(this, pipl))
  }

  def exitPipl(pipl: Pipeline): Unit = {
    listeners.foreach(_.exitPipl(this, pipl))
  }

  def enterFunc(func: PtolemyFunction): Unit = {
    listeners.foreach(_.enterFunc(this, func))
  }

  def exitFunc(func: PtolemyFunction): Unit = {
    listeners.foreach(_.exitFunc(this, func))
  }

  def enterMaths(maths: Maths): Unit = {
    listeners.foreach(_.exitMaths(this, maths))
  }

  def exitMaths(maths: Maths): Unit = {
    listeners.foreach(_.exitMaths(this, maths))
  }
}
