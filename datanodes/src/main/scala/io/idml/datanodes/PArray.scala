package io.idml.datanodes

import io.idml.{PtolemyArray, PtolemyValue}

import scala.collection.mutable

object PArray {
  val empty = PArray()

  /** Construct an array from zero or more items */
  def apply(items: PtolemyValue*): PArray = {
    PArray(mutable.Buffer(items: _*))
  }

  /** Extractor for PArray */
  def unapply(value: PtolemyValue): Option[Seq[PtolemyValue]] = value match {
    case arr: PArray => Some(arr.items)
    case _           => None
  }
}

/** The standard implementation of an array. Encapsulates a mutable array buffer */
case class PArray(var items: mutable.Buffer[PtolemyValue]) extends PtolemyArray {

  /** Clone this structure by making a deep copy of every element */
  override def deepCopy: PArray = new PArray(items.map(_.deepCopy))
}
