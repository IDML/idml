package io.idml

import io.idml.datanodes.PBool

import scala.collection.mutable

/** The IdmlValue that represents objects */
abstract class IdmlObject extends IdmlValue {

  def formatValue: mutable.SortedMap[String, IdmlValue] = fields

  /** The underlying field container for this object */
  def fields: mutable.SortedMap[String, IdmlValue]

  override def equals(o: Any): Boolean = o match {
    case n: IdmlObject => n.fields == fields
    case _             => false
  }

  override def hashCode(): Int = fields.hashCode()

  /** Iterate over values without keys */
  override def iterator: Iterator[IdmlValue] = fields.values.iterator

  /** Get fields if present */
  override def get(name: String): IdmlValue = {
    fields.getOrElse(name, MissingField)
  }

  /** Remove fields if able */
  override def remove(name: String) {
    fields.remove(name)
  }

  /** True if we have no fields */
  override def isEmpty: PBool = PBool(fields.isEmpty)

  /** No-op as we are already an object */
  // scalastyle:off method.name
  override def `object`(): IdmlObject = this
  // scalastyle:on method.name

  override def toStringOption: Option[String] =
    Some("{" + fields.flatMap { case (k, v) => v.toStringOption.map(k -> _) }.map { case (k, v) => s""""$k":$v""" }.mkString(",") + "}")
}
