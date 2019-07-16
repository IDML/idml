package io.idml

import io.idml.datanodes.PBool

import scala.collection.mutable

/** The PtolemyValue that represents objects */
abstract class PtolemyObject extends PtolemyValue {

  def formatValue: mutable.SortedMap[String, PtolemyValue] = fields

  /** The underlying field container for this object */
  def fields: mutable.SortedMap[String, PtolemyValue]

  override def equals(o: Any): Boolean = o match {
    case n: PtolemyObject => n.fields == fields
    case _                => false
  }

  override def hashCode(): Int = fields.hashCode()

  /** Iterate over values without keys */
  override def iterator: Iterator[PtolemyValue] = fields.values.iterator

  /** Get fields if present */
  override def get(name: String): PtolemyValue = {
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
  override def `object`(): PtolemyObject = this
  // scalastyle:on method.name

  override def toStringOption: Option[String] =
    Some("{" + fields.flatMap{case (k, v) => v.toStringOption.map(k -> _)}.map{case (k, v) => s""""$k":$v"""}.mkString(",") + "}")
}
