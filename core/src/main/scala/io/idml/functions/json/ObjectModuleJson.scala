package io.idml.functions.json

import io.idml._
import io.idml.ast.Pipeline
import io.idml.functions.IdmlFunction0

class ObjectModuleJson(json: IdmlJson) {
  def serialize(input: IdmlValue): IdmlValue = input match {
    case o: IdmlObject => IdmlValue(json.compact(o))
    case _             => InvalidCaller
  }

  val serializeFunction: IdmlFunction0 = new IdmlFunction0 {
    override protected def apply(cursor: IdmlValue): IdmlValue = serialize(cursor)
    override def name: String                                  = "serialize"
  }

  def parseJson(input: IdmlValue): IdmlValue = input match {
    case s: IdmlString => json.parseEither(s.value).getOrElse(InvalidCaller)
    case _             => InvalidCaller
  }

  val parseJsonFunction: IdmlFunction0 = new IdmlFunction0 {
    override protected def apply(cursor: IdmlValue): IdmlValue =
      parseJson(cursor)
    override def name: String = "parseJson"
  }
}
