package io.idml.datanodes.modules

import io.idml._

import scala.annotation.tailrec

trait NavigationModule {
  this: IdmlValue =>

  /** It's possible to get an item by its index */
  def get(index: Int): IdmlValue = NoIndex

  /** Fetch a field by name */
  def get(name: String): IdmlValue = NoFields

  /** It's possible to get an item by its index or field name */
  def get(index: IdmlValue): IdmlValue =
    index match {
      case str: IdmlString => get(str.toStringValue)
      case int: IdmlInt    => get(int.toIntValue)
      case _               => InvalidParameters
    }

  def deleted(): IdmlValue = Deleted

  /** Nested path removal. Will leave empty arrays and objects lingering */
  @tailrec
  final def remove(path: List[String]): Unit =
    path match {
      case Nil          =>
        throw new IllegalArgumentException("Cannot remove an empty path")
      case head :: Nil  => remove(head)
      case head :: tail => get(head).remove(tail)
    }

  /** Remove a path. Will remove empty arrays and objects */
  final def removeWithoutEmpty(path: List[String]): Unit =
    path match {
      case Nil          =>
        throw new IllegalArgumentException("Cannot remove an empty path")
      case head :: Nil  => remove(head)
      case head :: tail =>
        val child = get(head)

        // Apply the remove method recursively
        child.remove(tail)

        // Don't leave an empty array or object lingering
        if (child.isEmpty.value) {
          remove(head)
        }
    }

  /** It's possible to slice a range of values by index */
  def slice(from: Option[Int], to: Option[Int]): IdmlValue = NoIndex

  /** Slice can be called by users */
  def slice(from: IdmlValue, to: IdmlValue): IdmlValue =
    slice(from.toIntOption, to.toIntOption)

  /** Find the index of an item */
  def indexOf(value: IdmlValue): IdmlValue = NoIndex

}
