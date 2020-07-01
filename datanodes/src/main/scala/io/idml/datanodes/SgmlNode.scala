package io.idml.datanodes

import io.idml.{IdmlArray, IdmlObject, IdmlValue}

import scala.collection.mutable

case class SgmlNode(name: String, items: mutable.Buffer[IdmlValue], override val attributes: Map[String, String], body: String) extends IdmlObject with IdmlArray with CompositeValue {
  lazy val contents: mutable.Map[String, IdmlValue] = mutable.Map(attributes.toList.map { case (k, v) => k -> IdmlValue(v)}:_*)

  def fields: mutable.Map[String, IdmlValue] = contents
}
