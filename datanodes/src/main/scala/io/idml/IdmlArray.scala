package io.idml

import io.idml.datanodes.{CompositeValue, IArray, IBool, IInt}

import scala.collection.mutable
import scala.util.Try

/** A structure that has array-like behaviour */
trait IdmlArray extends IdmlValue with CompositeValue {

  def formatValue: mutable.Buffer[IdmlValue] = items

  /** The underlying items array */
  def items: mutable.Buffer[IdmlValue]

  /** Compare the items in this array to another array */
  override def equals(o: Any): Boolean = o match {
    case n: IdmlArray => n.items.filterNot(_.isNothing.value) == items.filterNot(_.isNothing.value)
    case _            => false
  }

  /** Return the hashcode for this array. Equal to the underlying items array */
  override def hashCode(): Int = items.hashCode()

  /** Wrap an index so we can support negatives but overflows should always return nothing */
  protected def wrapIndex(index: Int, size: Int): Int = {
    if (index < 0) {
      size + index
    } else {
      index
    }
  }

  /** Is the array empty? */
  override def isEmpty: IBool = IBool(items.isEmpty)

  /** Get the number of items in the array */
  def size: Int = items.size

  /** Find the index of an item */
  override def indexOf(value: IdmlValue): IdmlValue =
    IInt(items.indexOf(value))

  /** Get items within an index range slice slice */
  override def slice(from: Option[Int], to: Option[Int]): IdmlArray = {
    new IArray(items.slice(from.getOrElse(0), to.getOrElse(items.size)))
  }

  /** It's possible to get an item by its index */
  override def get(index: IdmlValue): IdmlValue =
    index.toIntOption.map(get).getOrElse(NoIndex)

  /** Get an item by its index */
  override def get(index: Int): IdmlValue = {
    // FIXME not good =)
    Try(items(wrapIndex(index, items.size))).getOrElse(MissingIndex)
  }

  override def toStringOption: Option[String] = Some("[" + items.flatMap(_.toStringOption).mkString(",") + "]")
}
