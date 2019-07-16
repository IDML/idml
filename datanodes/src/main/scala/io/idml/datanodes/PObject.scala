package io.idml.datanodes

import io.idml.{PtolemyArray, PtolemyObject, PtolemyValue}

import scala.collection.mutable
import scala.util.Try

/** The empty PObject */
object PObject {

  /** Create a PObject from a variable number of parameters */
  def apply(fields: (String, PtolemyValue)*): PObject = {
    PObject(mutable.SortedMap(fields: _*))
  }
}

/** The default PtolemyValue implementation for an object */
case class PObject(fields: mutable.SortedMap[String, PtolemyValue]) extends PtolemyObject {

  /** Create a copy of this object that can be safely modified */
  override def deepCopy: PObject =
    new PObject(fields.map { case (k, v) => (k, v.deepCopy) })

  private def internalDeepMerge(a: PtolemyValue, b: PtolemyValue): PtolemyValue = {
    (a, b) match {
      case (a: PArray, b: PArray) =>
        val acopy = a.deepCopy
        val bcopy = b.deepCopy
        val results = acopy.items.indices
          .flatMap { i =>
            Try { bcopy.items(i) }.toOption.map { bv =>
              internalDeepMerge(acopy.items(i), bv)
            }
          }
          .toBuffer[PtolemyValue]
        results.appendAll(bcopy.items.slice(acopy.items.size, bcopy.items.size))
        PArray(results)
      case (a: PtolemyObject, b: PtolemyObject) =>
        val keys  = a.fields.keySet ++ b.fields.keySet
        val acopy = a.deepCopy.asInstanceOf[PtolemyObject]
        val bcopy = b.deepCopy.asInstanceOf[PtolemyObject]
        val fs = keys.toList.map(k => (k, acopy.fields.get(k), bcopy.fields.get(k))).map {
          case (k, Some(v1), Some(v2)) =>
            k -> internalDeepMerge(v1, v2)
          case (k, Some(v1), None) =>
            k -> v1
          case (k, None, Some(v2)) =>
            k -> v2
          case (_, None, None) =>
            throw new Throwable("This can't happen because we only iterated keys that are in both objects")
        }
        PObject(fs: _*)
      case (a: PtolemyValue, b: PtolemyValue) =>
        b
    }
  }

  def deepMerge(other: PObject): PObject = {
    internalDeepMerge(this, other).asInstanceOf[PObject]
  }
}
