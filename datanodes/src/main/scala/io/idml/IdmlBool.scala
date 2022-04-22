package io.idml

/** The IdmlValue that contains boolean values */
trait IdmlBool extends IdmlValue {

  def formatValue: Boolean = value

  override def bool(): IdmlBool = this

  /** The boolean value of this IdmlValue */
  def value: Boolean

  override def equals(o: Any): Boolean =
    o match {
      case n: IdmlBool => n.value == value
      case _           => false
    }

  override def toBoolOption: Some[Boolean] = Some(value)

  override def hashCode(): Int = value.hashCode()

  override def toStringOption: Option[String] = if (value) Some("true") else Some("false")

}
