package io.idml.datanodes.modules

import cats.data.NonEmptyList
import io.idml.datanodes._
import io.idml._

import scala.collection.mutable

trait ArrayModule {
  this: IdmlValue =>

  def wrapArray(): IArray = IArray(this)

  def combinations(size: IdmlValue): IdmlValue = (this, size) match {
    case (a: IdmlArray, s: IdmlInt) => IArray(a.items.combinations(s.value.toInt).map(xs => IArray(xs)).toBuffer[IdmlValue])
    case (a: IdmlArray, _)          => InvalidParameters
    case _                          => InvalidCaller
  }

  def empty(): IdmlValue = this match {
    case a: IdmlArray  => a.isEmpty
    case s: IdmlString => s.isEmpty
    case _             => InvalidCaller
  }

  def unique(): IdmlValue = this match {
    case a: IdmlArray => IArray(a.items.distinct)
    case _            => InvalidCaller
  }

  def sort(): IdmlValue = this match {
    case a: IdmlArray => IArray(a.items.sorted)
    case _            => InvalidCaller
  }

  def min(): IdmlValue = this match {
    case a: IdmlArray => NonEmptyList.fromList(a.items.sorted.toList).map(_.toList.min).getOrElse(NoFields)
    case _            => InvalidCaller
  }

  def max(): IdmlValue = this match {
    case a: IdmlArray => NonEmptyList.fromList(a.items.sorted.toList).map(_.toList.max).getOrElse(NoFields)
    case _            => InvalidCaller
  }

  private def extractDouble(v: IdmlValue): Option[Double] = v match {
    case IInt(i)    => Some(i.toDouble)
    case IDouble(d) => Some(d)
    case _          => None
  }

  def average(): IdmlValue = this match {
    case a: IdmlArray if a.size == 0 => InvalidCaller
    case a: IdmlArray                => IDouble(a.items.flatMap(extractDouble).sum / a.items.size)
    case _                           => InvalidCaller
  }

  def median(): IdmlValue = this match {
    case a: IdmlArray if a.size == 0 => InvalidCaller
    case a: IdmlArray => {
      a.items.flatMap(x => extractDouble(x).map((x, _))).sortBy(_._2) match {
        case as if as.size % 2 == 0 => IDouble((as(as.size / 2)._2 + as((as.size / 2) - 1)._2) / 2)
        case as if as.size % 2 != 0 => as(Math.floor(as.size / 2).toInt)._1
      }
    }
    case _ => InvalidCaller
  }

  def variance(): IdmlValue = this match {
    case a: IdmlArray if a.size == 0 => InvalidCaller
    case a: IdmlArray => {
      val items       = a.items.flatMap(extractDouble)
      val mean        = items.sum / items.size
      val differences = items.map(x => Math.pow(x - mean, 2))
      IDouble(differences.sum / differences.size)
    }
    case _ => InvalidCaller
  }

  def stdDev(): IdmlValue = this match {
    case a: IdmlArray if a.size == 0 => InvalidCaller
    case a: IdmlArray => {
      val items       = a.items.flatMap(extractDouble)
      val mean        = items.sum / items.size
      val differences = items.map(x => Math.pow(x - mean, 2))
      IDouble(Math.sqrt(differences.sum / differences.size))
    }
    case _ => InvalidCaller
  }

  def softmax(): IdmlValue = this match {
    case a: IdmlArray if a.size == 0 => IArray(mutable.Buffer.empty[IdmlValue])
    case a: IdmlArray => {
      val as    = a.items.flatMap(extractDouble).map(x => Math.exp(x))
      val asSum = as.sum
      IArray(as.map(x => x / asSum).map(IDouble(_)).toBuffer[IdmlValue])
    }
    case _ => InvalidCaller
  }

  def flatten(): IdmlValue = this match {
    case a: IdmlArray =>
      IArray(a.items.flatMap { i =>
        i match {
          case arr: IdmlArray =>
            arr.items
          case p: IdmlValue =>
            mutable.Buffer(p)
        }
      })
    case _ => InvalidCaller
  }

  def flatten(depth: IdmlValue): IdmlValue = (this, depth) match {
    case (a: IdmlValue, i: IInt) =>
      (1L to i.value.toLong).foldLeft(a) { (x, y) =>
        x.flatten()
      }
    case _ => InvalidCaller
  }

  def zip(other: IdmlValue): IdmlValue = (this, other) match {
    case (a: IdmlArray, b: IdmlArray) =>
      IArray(a.items.zip(b.items).map { case (l, r) => IArray(l, r) }.toBuffer[IdmlValue])
    case (_: IdmlArray, _) =>
      InvalidParameters
    case (_, _: IdmlArray) =>
      InvalidCaller
    case (_, _) =>
      InvalidCaller
  }

  def enumerate(): IdmlValue = this match {
    case (a: IdmlArray) =>
      IArray(a.items.zipWithIndex.map { case (item, idx) => IArray(IdmlValue(idx), item) }.toBuffer[IdmlValue])
    case _ =>
      InvalidCaller
  }

  def union(other: IdmlValue): IdmlValue = (this, other) match {
    case (a: IdmlArray, b: IdmlArray) => IArray(a.items.toSet.union(b.items.toSet).toBuffer)
    case (_: IdmlArray, _) =>
      InvalidParameters
    case (_, _: IdmlArray) =>
      InvalidCaller
    case (_, _) =>
      InvalidCaller
  }

  def intersect(other: IdmlValue): IdmlValue = (this, other) match {
    case (a: IdmlArray, b: IdmlArray) => IArray(a.items.toSet.intersect(b.items.toSet).toBuffer)
    case (_: IdmlArray, _) =>
      InvalidParameters
    case (_, _: IdmlArray) =>
      InvalidCaller
    case (_, _) =>
      InvalidCaller
  }

  def diff(other: IdmlValue): IdmlValue = (this, other) match {
    case (a: IdmlArray, b: IdmlArray) => IArray(a.items.toSet.diff(b.items.toSet).toBuffer)
    case (_: IdmlArray, _) =>
      InvalidParameters
    case (_, _: IdmlArray) =>
      InvalidCaller
    case (_, _) =>
      InvalidCaller
  }

  import cats._, cats.implicits._, cats.syntax._

  def combineAll(): IdmlValue = this match {
    case ar: IdmlArray =>
      IArray(ar.items.filter(i => !i.isNothingValue)) match {
        case a: IdmlArray if a.items.forall(_.isArray.value) =>
          IArray(a.items.asInstanceOf[mutable.Buffer[IdmlArray]].map(_.items).flatten)
        case a: IdmlArray if a.items.forall(_.isObject.value) =>
          a.items.asInstanceOf[mutable.Buffer[IdmlObject]].foldLeft(IObject()) { case (o, i) => o.deepMerge(i) }
        case a: IdmlArray if a.items.forall(_.isString.value) =>
          IString(a.items.asInstanceOf[mutable.Buffer[IdmlString]].map(_.value).toList.combineAll)
        case a: IdmlArray if a.items.forall(_.isInt.value) =>
          IInt(a.items.asInstanceOf[mutable.Buffer[IdmlInt]].map(_.value).toList.combineAll)
        case a: IdmlArray if a.items.forall(_.isFloat.value) =>
          IDouble(a.items.asInstanceOf[mutable.Buffer[IdmlDouble]].map(_.value).toList.combineAll)
        case _ => InvalidCaller
      }
    case _ => InvalidCaller
  }
}
