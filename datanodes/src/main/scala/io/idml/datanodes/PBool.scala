package io.idml.datanodes

import io.idml.IdmlBool

/** The default IdmlValue implementation of a boolean */
case class PBool(value: Boolean) extends IdmlBool {

  // scalastyle:off method.name
  def ||(r: PBool): Boolean = value || r.value
  // scalastyle:on method.name

}

/** The PBool for true */
object PTrue extends PBool(true)

/** The PBool for false */
object PFalse extends PBool(false)
