package io.idml

/** Returned when there was no data. See this as equivalent to None or null */
trait PtolemyNothing extends PtolemyValue

/** Returned when a cast function rejected input because of its type. e.g. 123.csv() */
case object CastUnsupported extends PtolemyNothing

/**
  * Returned when a user supplied date format is invalid i.e. it's not a string or is an invalid format
  */
case object BadDateFormat extends PtolemyNothing

/** Returned when a cast function rejected input because of its value. e.g. "apples".email()  */
case object CastFailed extends PtolemyNothing

/** Returned when an item in an array was missing */
case object MissingIndex extends PtolemyNothing

/** Returned when a field in an object was missing */
case object MissingField extends PtolemyNothing

/** Returned when we attempted an index or slice operation on something that wasn't an array */
case object NoIndex extends PtolemyNothing

/** Returned when we attempted a get operation on something without fields */
case object NoFields extends PtolemyNothing

/** Returned when a coalesce didn't find anything */
case object EmptyCoalesce extends PtolemyNothing

/** Returned when a predicate evaluated to false, filtering out a value */
case object Filtered extends PtolemyNothing

/** Returned when a function's args were invalid */
case object InvalidParameters extends PtolemyNothing

/** Returned when a function's caller was invalid */
case object InvalidCaller extends PtolemyNothing

/** Returned when a requirement has failed */
case class FailedRequirement(reason: PtolemyNothing) extends PtolemyNothing

/** A function has nullified this field */
case object NoOp extends PtolemyNothing

/* Used to delete a variable */
case object Deleted extends PtolemyNothing
