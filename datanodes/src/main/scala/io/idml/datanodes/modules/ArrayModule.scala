package io.idml.datanodes.modules

import cats.data.NonEmptyList
import io.idml.datanodes._
import io.idml._

import scala.collection.mutable

trait ArrayModule {
  this: PtolemyValue =>

  def wrapArray(): PArray = PArray(this)

  def combinations(size: PtolemyValue): PtolemyValue = (this, size) match {
    case (a: PtolemyArray, s: PtolemyInt) => PArray(a.items.combinations(s.value.toInt).map(xs => PArray(xs)).toBuffer[PtolemyValue])
    case (a: PtolemyArray, _)             => InvalidParameters
    case _                                => InvalidCaller
  }

  def empty(): PtolemyValue = this match {
    case a: PtolemyArray  => a.isEmpty
    case s: PtolemyString => s.isEmpty
    case _                => InvalidCaller
  }

  def unique(): PtolemyValue = this match {
    case a: PtolemyArray => PArray(a.items.distinct)
    case _               => InvalidCaller
  }

  def sort(): PtolemyValue = this match {
    case a: PtolemyArray => PArray(a.items.sorted)
    case _               => InvalidCaller
  }

  def min(): PtolemyValue = this match {
    case a: PtolemyArray => NonEmptyList.fromList(a.items.sorted.toList).map(_.toList.min).getOrElse(NoFields)
    case _               => InvalidCaller
  }

  def max(): PtolemyValue = this match {
    case a: PtolemyArray => NonEmptyList.fromList(a.items.sorted.toList).map(_.toList.max).getOrElse(NoFields)
    case _               => InvalidCaller
  }

  private def extractDouble(v: PtolemyValue): Option[Double] = v match {
    case PInt(i)    => Some(i.toDouble)
    case PDouble(d) => Some(d)
    case _          => None
  }

  def average(): PtolemyValue = this match {
    case a: PtolemyArray if a.size == 0 => InvalidCaller
    case a: PtolemyArray                => PDouble(a.items.flatMap(extractDouble).sum / a.items.size)
    case _                              => InvalidCaller
  }

  def median(): PtolemyValue = this match {
    case a: PtolemyArray if a.size == 0 => InvalidCaller
    case a: PtolemyArray => {
      a.items.flatMap(x => extractDouble(x).map((x, _))).sortBy(_._2) match {
        case as if as.size % 2 == 0 => PDouble((as(as.size / 2)._2 + as((as.size / 2) - 1)._2) / 2)
        case as if as.size % 2 != 0 => as(Math.floor(as.size / 2).toInt)._1
      }
    }
    case _ => InvalidCaller
  }

  def variance(): PtolemyValue = this match {
    case a: PtolemyArray if a.size == 0 => InvalidCaller
    case a: PtolemyArray => {
      val items       = a.items.flatMap(extractDouble)
      val mean        = items.sum / items.size
      val differences = items.map(x => Math.pow(x - mean, 2))
      PDouble(differences.sum / differences.size)
    }
    case _ => InvalidCaller
  }

  def stdDev(): PtolemyValue = this match {
    case a: PtolemyArray if a.size == 0 => InvalidCaller
    case a: PtolemyArray => {
      val items       = a.items.flatMap(extractDouble)
      val mean        = items.sum / items.size
      val differences = items.map(x => Math.pow(x - mean, 2))
      PDouble(Math.sqrt(differences.sum / differences.size))
    }
    case _ => InvalidCaller
  }

  def softmax(): PtolemyValue = this match {
    case a: PtolemyArray if a.size == 0 => PArray(mutable.Buffer.empty[PtolemyValue])
    case a: PtolemyArray => {
      val as    = a.items.flatMap(extractDouble).map(x => Math.exp(x))
      val asSum = as.sum
      PArray(as.map(x => x / asSum).map(PDouble(_)).toBuffer[PtolemyValue])
    }
    case _ => InvalidCaller
  }

  def flatten(): PtolemyValue = this match {
    case a: PtolemyArray =>
      PArray(a.items.flatMap { i =>
        i match {
          case arr: PtolemyArray =>
            arr.items
          case p: PtolemyValue =>
            mutable.Buffer(p)
        }
      })
    case _ => InvalidCaller
  }

  def flatten(depth: PtolemyValue): PtolemyValue = (this, depth) match {
    case (a: PtolemyValue, i: PInt) =>
      (1L to i.value.toLong).foldLeft(a) { (x, y) =>
        x.flatten()
      }
    case _ => InvalidCaller
  }

  def zip(other: PtolemyValue): PtolemyValue = (this, other) match {
    case (a: PtolemyArray, b: PtolemyArray) =>
      PArray(a.items.zip(b.items).map { case (l, r) => PArray(l, r) }.toBuffer[PtolemyValue])
    case (_: PtolemyArray, _) =>
      InvalidParameters
    case (_, _: PtolemyArray) =>
      InvalidCaller
    case (_, _) =>
      InvalidCaller
  }

  def enumerate(): PtolemyValue = this match {
    case (a: PtolemyArray) =>
      PArray(a.items.zipWithIndex.map { case (item, idx) => PArray(PtolemyValue(idx), item) }.toBuffer[PtolemyValue])
    case _ =>
      InvalidCaller
  }

  import cats._, cats.implicits._, cats.syntax._

  def combineAll(): PtolemyValue = this match {
    case ar: PtolemyArray =>
      PArray(ar.items.filter(i => !i.isNothingValue)) match {
        case a: PtolemyArray if a.items.forall(_.isArray.value) =>
          PArray(a.items.asInstanceOf[mutable.Buffer[PtolemyArray]].map(_.items).flatten)
        case a: PtolemyArray if a.items.forall(_.isObject.value) =>
          a.items.asInstanceOf[mutable.Buffer[PtolemyObject]].foldLeft(PObject()) { case (o, i) => PObject(o.fields ++ i.fields) }
        case a: PtolemyArray if a.items.forall(_.isString.value) =>
          PString(a.items.asInstanceOf[mutable.Buffer[PtolemyString]].map(_.value).toList.combineAll)
        case a: PtolemyArray if a.items.forall(_.isInt.value) =>
          PInt(a.items.asInstanceOf[mutable.Buffer[PtolemyInt]].map(_.value).toList.combineAll)
        case a: PtolemyArray if a.items.forall(_.isFloat.value) =>
          PDouble(a.items.asInstanceOf[mutable.Buffer[PtolemyDouble]].map(_.value).toList.combineAll)
        case _ => InvalidCaller
      }
    case _ => InvalidCaller
  }
}
