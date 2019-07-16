package io.idml.functions.json

import io.idml._
import io.idml.ast.Pipeline
import io.idml.functions.PtolemyFunction0

class ObjectModuleJson(json: PtolemyJson) {
  def serialize(input: PtolemyValue): PtolemyValue = input match {
    case o: PtolemyObject => PtolemyValue(json.compact(o))
    case _                => InvalidCaller
  }

  val serializeFunction: PtolemyFunction0 = new PtolemyFunction0 {
    override protected def apply(cursor: PtolemyValue): PtolemyValue = serialize(cursor)
    override def name: String = "serialize"
  }

  def parseJson(input: PtolemyValue): PtolemyValue = input match {
    case s: PtolemyString => json.parseEither(s.value).getOrElse(InvalidCaller)
    case _                => InvalidCaller
  }

  val parseJsonFunction: PtolemyFunction0 = new PtolemyFunction0 {
    override protected def apply(cursor: PtolemyValue): PtolemyValue =
      parseJson(cursor)
    override def name: String = "parseJson"
  }
}
