package io.idml.utils

import io.idml.{IdmlArray, IdmlObject, IdmlValue}

/** A utility for doing a deep comparison of PValues and producing meaningful feedback should they
  * not be the same
  */
object PValueComparison {

  /** Perform a deep comparison of two PValues */
  def assertEqual(left: IdmlValue, right: IdmlValue, path: List[String] = Nil): Unit = {
    (left, right) match {
      case (l: IdmlObject, r: IdmlObject) =>
        l.fields.keys.foreach { k =>
          assertEqual(l.get(k), r.get(k), k :: path)
        }
        require(
          l.fields.keys == r.fields.keys,
          s"${path.reverse.mkString(".")} actual: ${l.fields}, expected: ${r.fields}")
      case (l: IdmlArray, r: IdmlArray)   =>
        l.items.zip(r.items).zipWithIndex.foreach { case ((ll: IdmlValue, rr: IdmlValue), i: Int) =>
          assertEqual(ll, rr, i.toString :: path)
        }
        require(
          l.items.length == r.items.length,
          s"${path.reverse.mkString(".")} actual: ${l.items}, expected: ${r.items}")
      case (l, r)                         =>
        require(l == r, s"${path.reverse.mkString(".")} actual: $l, expected: $r")
    }
  }
}
