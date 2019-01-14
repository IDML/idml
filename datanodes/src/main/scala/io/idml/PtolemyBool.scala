package io.idml

/** The PtolemyValue that contains boolean values */
trait PtolemyBool extends PtolemyValue {

  def formatValue: Boolean = value

  override def bool(): PtolemyBool = this

  /** The boolean value of this PtolemyValue */
  def value: Boolean

  override def equals(o: Any): Boolean = o match {
    case n: PtolemyBool => n.value == value
    case _              => false
  }

  override def toBoolOption: Some[Boolean] = Some(value)

  override def hashCode(): Int = value.hashCode()

  override def toStringOption: Option[String] = if (value) Some("true") else Some("false")

}
