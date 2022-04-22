package io.idml

/** Base class for an exception */
// scalastyle:off null
abstract class IdmlException(msg: String = null, ex: Exception = null)
    extends RuntimeException(msg, ex)
// scalastyle:on null

/** The exception that is thrown when we couldn't resolve a function with this name and number of
  * parameters
  */
class UnknownFunctionException(msg: String) extends IdmlException(msg)

/** The exception that is thrown when we asked to apply() a block that doesn't exist */
class UnknownBlockException(msg: String) extends IdmlException(msg)

/** Thrown when there's no way to load functions.. this is probably a misconfiguration */
class NoFunctionResolversLoadedException(msg: String) extends IdmlException(msg)
