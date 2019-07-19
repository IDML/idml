// scalastyle:off number.of.methods
package io.idml.datanodes.modules

import io.idml.datanodes.{IBool, IFalse, ITrue}
import io.idml.{
  CastUnsupported,
  FailedRequirement,
  IdmlArray,
  IdmlBool,
  IdmlDouble,
  IdmlInt,
  IdmlNothing,
  IdmlNull,
  IdmlObject,
  IdmlString,
  IdmlValue
}

/** This trait adds basic schema functions */
trait SchemaModule {
  this: IdmlValue =>

  // scalastyle:off method.name
  /** Transform this field into an object */
  def `object`(): IdmlValue = this match {
    case _: IdmlObject | _: IdmlNothing => this
    case _                              => CastUnsupported
  }
  // scalastyle:on method.name

  /** Transform this field into a floating-point number */
  def float(): IdmlValue = this match {
    case _: IdmlDouble | _: IdmlNothing => this
    case _                              => CastUnsupported
  }

  /** Transform this field into a natural number */
  def int(): IdmlValue = this match {
    case _: IdmlInt | _: IdmlNothing => this
    case _                           => CastUnsupported
  }

  /** Transform this field into a boolean */
  def bool(): IdmlValue = this match {
    case _: IdmlBool | _: IdmlNothing => this
    case _                            => CastUnsupported
  }

  /** Transform this field into a float */
  def double(): IdmlValue = this match {
    case _: IdmlDouble | _: IdmlNothing => this
    case _                              => CastUnsupported
  }

  /** Transform this field into an array */
  def array(): IdmlValue = this match {
    case _: IdmlArray | _: IdmlNothing => this
    case _                             => CastUnsupported
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
  def iterator: Iterator[IdmlValue] = Iterator.empty

  /** Is this true? */
  def isTrue: IBool = IBool(isTrueValue)

  /** Is this true? */
  def isTrueValue: Boolean = this == ITrue

  /** Is this false? */
  def isFalse: IBool = IBool(isFalseValue)

  /** Is this false? */
  def isFalseValue: Boolean = this == IFalse

  /** Is this value null? */
  def isNull: IBool = IBool(isNullValue)

  /** Is this value null? */
  def isNullValue: Boolean = this == IdmlNull

  /** Is this value nothing? */
  def isNothing: IBool = IBool(isNothingValue)

  /** Is this value nothing? */
  def isNothingValue: Boolean = isInstanceOf[IdmlNothing]

  /** Is this a string? */
  def isString: IBool = IBool(isStringValue)

  /** Is this a string? */
  def isStringValue: Boolean = isInstanceOf[IdmlString]

  /** Is this an int? */
  def isInt: IBool = IBool(this.isInstanceOf[IdmlInt])

  /** Is this an array? */
  def isArray: IBool = IBool(this.isInstanceOf[IdmlArray])

  /** Is this an object? */
  def isObject: IBool = IBool(this.isInstanceOf[IdmlObject])

  /** Is this a float? */
  def isFloat: IBool = IBool(this.isInstanceOf[IdmlDouble])

  /** This is a form of type coercion similar to php's empty(..) function; false, null, missing
    * values, empty arrays and objects all return true for isEmpty */
  def isEmpty: IBool = IBool(isFalseValue || isNullValue || isNothingValue)

  /** Annotate any missing fields. This is useful later on for processes like schema matching */
  def required(): IdmlValue = this match {
    case reason: IdmlNothing => FailedRequirement(reason)
    case success: Any        => success
  }

  /** If the value is missing then use a default value instead */
  def default(value: IdmlValue): IdmlValue =
    if (this.isNothing || this.isNull) value else this

  /* alias for default */
  def orElse(value: IdmlValue): IdmlValue = this.default(value)

  /** Create a new version of this object that can be safely modified by other callers */
  def deepCopy: IdmlValue = this

  /** Convert to Json */
  //def json: String = IdmlJson.compact(this)
}
// scalastyle:on number.of.methods
