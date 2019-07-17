package io.idml.datanodes

import io.idml.{IdmlArray, IdmlValue}

import scala.collection.mutable

object PArray {
  val empty = PArray()

  /** Construct an array from zero or more items */
  def apply(items: IdmlValue*): PArray = {
    PArray(mutable.Buffer(items: _*))
  }

  /** Extractor for PArray */
  def unapply(value: IdmlValue): Option[Seq[IdmlValue]] = value match {
    case arr: PArray => Some(arr.items)
    case _           => None
  }

  def of(arr: Array[IdmlValue]): PArray = {
    PArray(arr.toBuffer)
  }
}

/** The standard implementation of an array. Encapsulates a mutable array buffer */
case class PArray(var items: mutable.Buffer[IdmlValue]) extends IdmlArray {

  /** Clone this structure by making a deep copy of every element */
  override def deepCopy: PArray = new PArray(items.map(_.deepCopy))
}
