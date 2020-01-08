package io.idml.datanodes

import io.idml.{IdmlArray, IdmlInt, IdmlObject, IdmlString, IdmlValue, InvalidParameters, MissingField}

import scala.collection.mutable

sealed trait IDomNode extends CompositeValue {
  def getText: IdmlString
}

case class IDomText(value: String) extends IdmlString with IDomNode {
  def getText: IdmlString = IString(value)
}

case class IDomElement(name: String, attrs: Map[String, String], children: List[IDomNode]) extends IdmlObject with IdmlArray with IDomNode {
  // if we're serializing to JSON this is the representation
  def fields: mutable.Map[String, IdmlValue] = mutable.Map(
    "name" -> IString(name),
    "attributes" -> IObject(attrs.mapValues(IString).toList:_*),
    "contents" -> IArray(children.toBuffer[IdmlValue])
  )
  def items: mutable.Buffer[IdmlValue] = children.toBuffer[IdmlValue]

  override lazy val attributes: IObject = IObject(attrs.toList.map { case (k, v) => k -> IString(v) }:_*)


  // if getting a name, we're actually going to filter the children
  override def get(name: String): IdmlValue = this.copy(children = children.filter {
    case e: IDomElement if e.name == name => true
    case _ => false
  })

  // delegate to object or array depending
  override def get(index: IdmlValue): IdmlValue = index match {
    case str: IdmlString => get(str.toStringValue)
    case int: IdmlInt    => get(int.toIntValue)
    case _               => InvalidParameters
  }

  def getText: IdmlString = children.map(_.getText).foldLeft(IString("")) { (a, b) => IString(a.value + b.value) }
}

