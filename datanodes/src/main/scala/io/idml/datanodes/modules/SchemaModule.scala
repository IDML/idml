// scalastyle:off number.of.methods
package io.idml.datanodes.modules

import io.idml.datanodes.{PBool, PFalse, PTrue}
import io.idml.{
  CastUnsupported,
  FailedRequirement,
  PtolemyArray,
  PtolemyBool,
  PtolemyDouble,
  PtolemyInt,
  PtolemyNothing,
  PtolemyNull,
  PtolemyObject,
  PtolemyString,
  PtolemyValue
}

/** This trait adds basic schema functions */
trait SchemaModule {
  this: PtolemyValue =>

  // scalastyle:off method.name
  /** Transform this field into an object */
  def `object`(): PtolemyValue = this match {
    case _: PtolemyObject | _: PtolemyNothing => this
    case _                                    => CastUnsupported
  }
  // scalastyle:on method.name

  /** Transform this field into a floating-point number */
  def float(): PtolemyValue = this match {
    case _: PtolemyDouble | _: PtolemyNothing => this
    case _                                    => CastUnsupported
  }

  /** Transform this field into a natural number */
  def int(): PtolemyValue = this match {
    case _: PtolemyInt | _: PtolemyNothing => this
    case _                                 => CastUnsupported
  }

  /** Transform this field into a boolean */
  def bool(): PtolemyValue = this match {
    case _: PtolemyBool | _: PtolemyNothing => this
    case _                                  => CastUnsupported
  }

  /** Transform this field into a float */
  def double(): PtolemyValue = this match {
    case _: PtolemyDouble | _: PtolemyNothing => this
    case _                                    => CastUnsupported
  }

  /** Transform this field into an array */
  def array(): PtolemyValue = this match {
    case _: PtolemyArray | _: PtolemyNothing => this
    case _                                   => CastUnsupported
  }

  /** Extract the underlying float value for this node if it has one, otherwise return a default */
  def toFloatValue: Float = toFloatOption.getOrElse(0f)

  /** Extract the underlying int value for this node if it has one, otherwise return a default */
  def toIntValue: Int = toIntOption.getOrElse(0)

  /** Extract the underlying long value for this node if it has one, otherwise return a default */
  def toLongValue: Long = toLongOption.getOrElse(0L)

  /** Extract the underlying boolean value for this node if it has one, otherwise return a default */
  def toBoolValue: Boolean = toBoolOption.getOrElse(false)

  /** Extract the underlying double value for this node if it has one, otherwise return a default */
  def toDoubleValue: Double = toDoubleOption.getOrElse(0.0)

  /** Extract the underlying string value for this node if it has one, otherwise return a default */
  def toStringValue: String = toStringOption.getOrElse("")

  /** Extract the underlying float value for this node if it has one, otherwise return none */
  def toFloatOption: Option[Float] = None

  /** Extract the underlying int value for this node if it has one, otherwise return none */
  def toIntOption: Option[Int] = None

  /** Extract the underlying long value for this node if it has one, otherwise return none */
  def toLongOption: Option[Long] = None

  /** Extract the underlying boolean value for this node if it has one, otherwise return none */
  def toBoolOption: Option[Boolean] = None

  /** Extract the underlying double value for this node if it has one, otherwise return none */
  def toDoubleOption: Option[Double] = None

  /** Extract the underlying string value for this node if it has one, otherwise return none */
  def toStringOption: Option[String] = None

  /** Iterate over the sub-components of this node */
  def iterator: Iterator[PtolemyValue] = Iterator.empty

  /** Is this true? */
  def isTrue: PBool = PBool(isTrueValue)

  /** Is this true? */
  def isTrueValue: Boolean = this == PTrue

  /** Is this false? */
  def isFalse: PBool = PBool(isFalseValue)

  /** Is this false? */
  def isFalseValue: Boolean = this == PFalse

  /** Is this value null? */
  def isNull: PBool = PBool(isNullValue)

  /** Is this value null? */
  def isNullValue: Boolean = this == PtolemyNull

  /** Is this value nothing? */
  def isNothing: PBool = PBool(isNothingValue)

  /** Is this value nothing? */
  def isNothingValue: Boolean = isInstanceOf[PtolemyNothing]

  /** Is this a string? */
  def isString: PBool = PBool(isStringValue)

  /** Is this a string? */
  def isStringValue: Boolean = isInstanceOf[PtolemyString]

  /** Is this an int? */
  def isInt: PBool = PBool(this.isInstanceOf[PtolemyInt])

  /** Is this an array? */
  def isArray: PBool = PBool(this.isInstanceOf[PtolemyArray])

  /** Is this an object? */
  def isObject: PBool = PBool(this.isInstanceOf[PtolemyObject])

  /** Is this a float? */
  def isFloat: PBool = PBool(this.isInstanceOf[PtolemyDouble])

  /** This is a form of type coercion similar to php's empty(..) function; false, null, missing
    * values, empty arrays and objects all return true for isEmpty */
  def isEmpty: PBool = PBool(isFalseValue || isNullValue || isNothingValue)

  /** Annotate any missing fields. This is useful later on for processes like schema matching */
  def required(): PtolemyValue = this match {
    case reason: PtolemyNothing => FailedRequirement(reason)
    case success: Any           => success
  }

  /** If the value is missing then use a default value instead */
  def default(value: PtolemyValue): PtolemyValue =
    if (this.isNothing || this.isNull) value else this

  /* alias for default */
  def orElse(value: PtolemyValue): PtolemyValue = this.default(value)

  /** Create a new version of this object that can be safely modified by other callers */
  def deepCopy: PtolemyValue = this

  /** Convert to Json */
  //def json: String = PtolemyJson.compact(this)
}
// scalastyle:on number.of.methods
