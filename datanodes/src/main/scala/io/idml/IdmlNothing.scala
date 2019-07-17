package io.idml

/** Returned when there was no data. See this as equivalent to None or null */
trait IdmlNothing extends IdmlValue

/** Returned when a cast function rejected input because of its type. e.g. 123.csv() */
case object CastUnsupported extends IdmlNothing

/**
  * Returned when a user supplied date format is invalid i.e. it's not a string or is an invalid format
  */
case object BadDateFormat extends IdmlNothing

/** Returned when a cast function rejected input because of its value. e.g. "apples".email()  */
case object CastFailed extends IdmlNothing

/** Returned when an item in an array was missing */
case object MissingIndex extends IdmlNothing

/** Returned when a field in an object was missing */
case object MissingField extends IdmlNothing

/** Returned when we attempted an index or slice operation on something that wasn't an array */
case object NoIndex extends IdmlNothing

/** Returned when we attempted a get operation on something without fields */
case object NoFields extends IdmlNothing

/** Returned when a coalesce didn't find anything */
case object EmptyCoalesce extends IdmlNothing

/** Returned when a predicate evaluated to false, filtering out a value */
case object Filtered extends IdmlNothing

/** Returned when a function's args were invalid */
case object InvalidParameters extends IdmlNothing

/** Returned when a function's caller was invalid */
case object InvalidCaller extends IdmlNothing

/** Returned when a requirement has failed */
case class FailedRequirement(reason: IdmlNothing) extends IdmlNothing

/** A function has nullified this field */
case object NoOp extends IdmlNothing

/* Used to delete a variable */
case object Deleted extends IdmlNothing
