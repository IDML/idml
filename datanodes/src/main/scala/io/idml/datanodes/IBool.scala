package io.idml.datanodes

import io.idml.IdmlBool

/** The default IdmlValue implementation of a boolean */
case class IBool(value: Boolean) extends IdmlBool {

  // scalastyle:off method.name
  def ||(r: IBool): Boolean = value || r.value
  // scalastyle:on method.name

}

/** The PBool for true */
object ITrue extends IBool(true)

/** The PBool for false */
object IFalse extends IBool(false)
