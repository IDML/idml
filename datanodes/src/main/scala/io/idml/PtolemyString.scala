package io.idml

import io.idml.datanodes._
import com.google.common.base.CharMatcher

import scala.util.Try

/** For a value that has string behaviours */
trait PtolemyString extends PtolemyValue {

  @deprecated(message = "Use toStringOption and related functions instead", since = "1.3.0")
  def formatValue: String = value

  /** The underlying string value */
  def value: String

  /** Compare a string to something else */
  override def equals(o: Any): Boolean = o match {
    case n: PtolemyString => n.value == value
    case _                => false
  }

  /** Try to parse this string as a float */
  override def float(): PtolemyValue with Product with Serializable = {
    Try(value.toDouble).map(new PDouble(_)).getOrElse(CastFailed)
  }

  /** Try to parse this string as an int */
  override def int(): PtolemyValue with Product with Serializable = {
    Try(value.toLong).map(new PInt(_)).getOrElse(CastFailed)
  }

  override def bool(): PtolemyValue with Product with Serializable = {
    value.toLowerCase match {
      case "true"  => PTrue
      case "false" => PFalse
      case "yes"   => PTrue
      case "no"    => PFalse
      case "1"     => PTrue
      case "0"     => PFalse
      case _       => CastFailed
    }
  }

  /** The hashcode */
  override def hashCode(): Int = value.hashCode()

  /** Lowercase this string */
  override def lowercase(): PtolemyValue = PtolemyValue(value.toLowerCase)

  /** Uppercase this string */
  override def uppercase(): PtolemyValue = PtolemyValue(value.toUpperCase)

  override def capitalize(): PtolemyValue = PtolemyValue(value.split(' ').map(_.capitalize).mkString(" "))

  override def strip(): PtolemyValue = PtolemyValue(CharMatcher.whitespace.trimFrom(value))

  /** Get the underlying string */
  override def toStringOption: Some[String] = Some(value)

  /** Empty if equal to "" */
  override def isEmpty: PBool = PBool(value.isEmpty)

  override def slice(from: Option[Int], to: Option[Int]): PtolemyString = {
    new PString(this.value.slice(from.getOrElse(0), to.getOrElse(this.value.length)))
  }
}
