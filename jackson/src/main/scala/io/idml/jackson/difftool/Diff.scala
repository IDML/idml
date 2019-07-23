package io.idml.jackson.difftool

import io.idml.datanodes.{IArray, IObject, IString}
import io.idml.{IdmlArray, IdmlObject, IdmlValue, MissingIndex}
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper

import scala.collection.mutable

/** Compares IdmlValue objects and creates a diff.
  *
  * This aims to be similar to the results of a text diff tool except it works on deeply nested object graphs.
  *
  * In order to do this we introduce a new Diff IdmlValue type which is equivalent to a json array of the form
  * ["__DIFF__", left, right] in each case where the left and right side do not match, or otherwise return the original
  * value if both sides match.
  *
  * diff( {x: {y: A, z: B}}, {x: {y: A, z: C}} ) = {x: {y: A, z: [__DIFF__, B, C] }}
  */
object Diff {

  val marker: IdmlValue = IString("__DIFF__")

  val mapper: ObjectMapper = new ObjectMapper()
    .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
    .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
    .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
    .registerModule(new DiffJacksonModule)

  /**
    * Determine if a value is a diff
    */
  def isDiff(value: IdmlValue): Boolean = value match {
    case IArray(Seq(this.marker, left, right)) => true
    case _                                     => false
  }

  /**
    * Compare two objects
    */
  def compareObjects(left: IdmlObject, right: IdmlObject): IdmlValue = {
    if (left == right) {
      left
    } else {
      val diffs = mutable.Map[String, IdmlValue]()

      left.fields.foreach {
        case (k: String, leftValue: IdmlValue) =>
          val rightValue = right.get(k)
          if (leftValue != rightValue) {
            diffs(k) = compare(leftValue, rightValue)
          } else {
            diffs(k) = leftValue
          }
      }

      right.fields.foreach {
        case (k: String, rightValue: IdmlValue) =>
          val leftValue = left.get(k)
          if (leftValue != rightValue) {
            diffs(k) = compare(leftValue, rightValue)
          } else {
            diffs(k) = leftValue
          }
      }

      IObject(diffs)
    }
  }

  /**
    * Compare two arrays
    */
  def compareArrays(left: IdmlArray, right: IdmlArray): IdmlValue = {
    if (left == right) {
      left
    } else {
      val diffs =
        left.items.zipAll(right.items, MissingIndex, MissingIndex).map(compare)
      IArray(diffs)
    }
  }

  /**
    * Compare two values in a tuple
    */
  def compare(tuple: (IdmlValue, IdmlValue)): IdmlValue = {
    compare(tuple._1, tuple._2)
  }

  /**
    * Compare two values
    */
  def compare(left: IdmlValue, right: IdmlValue): IdmlValue = {
    (left, right) match {
      case (left: IdmlObject, right: IdmlObject) =>
        compareObjects(left, right)
      case (left: IdmlArray, right: IdmlArray) =>
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
  def createDiff(left: IdmlValue, right: IdmlValue): IdmlValue = {
    IArray(marker, left, right)
  }

  /** Render a DataNode hierarchy as a pretty-printed json dom */
  def pretty(left: IdmlValue, right: IdmlValue): String = {
    pretty(compare(left, right))
  }

  /** Render a diff tree */
  def pretty(result: IdmlValue): String = {
    val writer = mapper.writerWithDefaultPrettyPrinter()
    writer.writeValueAsString(result)
  }

}
