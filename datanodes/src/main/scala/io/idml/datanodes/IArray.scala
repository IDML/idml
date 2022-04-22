package io.idml.datanodes

import io.idml.{IdmlArray, IdmlValue}

import scala.collection.mutable

object IArray {
  val empty = IArray()

  /** Construct an array from zero or more items */
  def apply(items: IdmlValue*): IArray = {
    IArray(mutable.Buffer(items: _*))
  }

  /** Extractor for PArray */
  def unapply(value: IdmlValue): Option[Seq[IdmlValue]] =
    value match {
      case arr: IArray => Some(arr.items.toSeq)
      case _           => None
    }

  def of(arr: Array[IdmlValue]): IArray = {
    IArray(arr.toBuffer)
  }
}

/** The standard implementation of an array. Encapsulates a mutable array buffer */
case class IArray(var items: mutable.Buffer[IdmlValue]) extends IdmlArray {

  /** Clone this structure by making a deep copy of every element */
  override def deepCopy: IArray = new IArray(items.map(_.deepCopy))
}
