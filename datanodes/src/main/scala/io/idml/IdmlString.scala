package io.idml

import io.idml.datanodes._
import com.google.common.base.CharMatcher

import scala.util.Try

/** For a value that has string behaviours */
trait IdmlString extends IdmlValue {

  @deprecated(message = "Use toStringOption and related functions instead", since = "1.3.0")
  def formatValue: String = value

  /** The underlying string value */
  def value: String

  /** Compare a string to something else */
  override def equals(o: Any): Boolean =
    o match {
      case n: IdmlString => n.value == value
      case _             => false
    }

  /** Try to parse this string as a float */
  override def float(): IdmlValue with Product with Serializable = {
    Try(value.toDouble).map(new IDouble(_)).getOrElse(CastFailed)
  }

  /** Try to parse this string as an int */
  override def int(): IdmlValue with Product with Serializable = {
    Try(value.toLong).map(new IInt(_)).getOrElse(CastFailed)
  }

  override def bool(): IdmlValue with Product with Serializable = {
    value.toLowerCase match {
      case "true"  => ITrue
      case "false" => IFalse
      case "yes"   => ITrue
      case "no"    => IFalse
      case "1"     => ITrue
      case "0"     => IFalse
      case _       => CastFailed
    }
  }

  /** The hashcode */
  override def hashCode(): Int = value.hashCode()

  /** Lowercase this string */
  override def lowercase(): IdmlValue = IdmlValue(value.toLowerCase)

  /** Uppercase this string */
  override def uppercase(): IdmlValue = IdmlValue(value.toUpperCase)

  override def capitalize(): IdmlValue = IdmlValue(value.split(' ').map(_.capitalize).mkString(" "))

  override def strip(): IdmlValue = IdmlValue(CharMatcher.whitespace.trimFrom(value))

  /** Get the underlying string */
  override def toStringOption: Some[String] = Some(value)

  /** Empty if equal to "" */
  override def isEmpty: IBool = IBool(value.isEmpty)

  override def slice(from: Option[Int], to: Option[Int]): IdmlString = {
    new IString(this.value.slice(from.getOrElse(0), to.getOrElse(this.value.length)))
  }
}
