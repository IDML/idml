package io.idml.jackson.difftool

import io.idml.datanodes.{PArray, PObject, PString}
import io.idml.{MissingIndex, PtolemyArray, PtolemyObject, PtolemyValue}
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper

import scala.collection.mutable

/** Compares PtolemyValue objects and creates a diff.
  *
  * This aims to be similar to the results of a text diff tool except it works on deeply nested object graphs.
  *
  * In order to do this we introduce a new Diff PtolemyValue type which is equivalent to a json array of the form
  * ["__DIFF__", left, right] in each case where the left and right side do not match, or otherwise return the original
  * value if both sides match.
  *
  * diff( {x: {y: A, z: B}}, {x: {y: A, z: C}} ) = {x: {y: A, z: [__DIFF__, B, C] }}
  */
object Diff {

  val marker: PtolemyValue = PString("__DIFF__")

  val mapper: ObjectMapper = new ObjectMapper()
    .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
    .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
    .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
    .registerModule(new DiffJacksonModule)

  /**
    * Determine if a value is a diff
    */
  def isDiff(value: PtolemyValue): Boolean = value match {
    case PArray(Seq(this.marker, left, right)) => true
    case _                                     => false
  }

  /**
    * Compare two objects
    */
  def compareObjects(left: PtolemyObject, right: PtolemyObject): PtolemyValue = {
    if (left == right) {
      left
    } else {
      val diffs = mutable.SortedMap[String, PtolemyValue]()

      left.fields.foreach {
        case (k: String, leftValue: PtolemyValue) =>
          val rightValue = right.get(k)
          if (leftValue != rightValue) {
            diffs(k) = compare(leftValue, rightValue)
          } else {
            diffs(k) = leftValue
          }
      }

      right.fields.foreach {
        case (k: String, rightValue: PtolemyValue) =>
          val leftValue = left.get(k)
          if (leftValue != rightValue) {
            diffs(k) = compare(leftValue, rightValue)
          } else {
            diffs(k) = leftValue
          }
      }

      PObject(diffs)
    }
  }

  /**
    * Compare two arrays
    */
  def compareArrays(left: PtolemyArray, right: PtolemyArray): PtolemyValue = {
    if (left == right) {
      left
    } else {
      val diffs =
        left.items.zipAll(right.items, MissingIndex, MissingIndex).map(compare)
      PArray(diffs)
    }
  }

  /**
    * Compare two values in a tuple
    */
  def compare(tuple: (PtolemyValue, PtolemyValue)): PtolemyValue = {
    compare(tuple._1, tuple._2)
  }

  /**
    * Compare two values
    */
  def compare(left: PtolemyValue, right: PtolemyValue): PtolemyValue = {
    (left, right) match {
      case (left: PtolemyObject, right: PtolemyObject) =>
        compareObjects(left, right)
      case (left: PtolemyArray, right: PtolemyArray) =>
        compareArrays(left, right)
      case _ if left != right =>
        createDiff(left, right)
      case _ =>
        left
    }
  }

  /**
    * Create a diff marker
    */
  def createDiff(left: PtolemyValue, right: PtolemyValue): PtolemyValue = {
    PArray(marker, left, right)
  }

  /** Render a DataNode hierarchy as a pretty-printed json dom */
  def pretty(left: PtolemyValue, right: PtolemyValue): String = {
    pretty(compare(left, right))
  }

  /** Render a diff tree */
  def pretty(result: PtolemyValue): String = {
    val writer = mapper.writerWithDefaultPrettyPrinter()
    writer.writeValueAsString(result)
  }

}
