package io.idml.datanodes

import javax.mail.internet.InternetAddress

import io.idml.{IdmlString, IdmlValue, MissingField}

/** Represents a valid Email */
class PEmail(email: InternetAddress) extends IdmlString with CompositeValue {

  /** Extract the address component */
  def address: IdmlValue = IdmlValue(addressValue)

  /** Extract the name component */
  def name: IdmlValue = IdmlValue(nameValue)

  /** Extract the username component */
  def username: IdmlValue = IdmlValue(usernameValue)

  /** Extract the domain component */
  def domain: IdmlValue = IdmlValue(domainValue)

  /** Extract the address component */
  def addressValue: String = email.getAddress

  /** Extract the name component */
  def nameValue: String = email.getPersonal

  /** Extract the username component */
  def usernameValue: String = email.getAddress.substring(0, splitPoint)

  /** Extract the domain component */
  def domainValue: String = email.getAddress.substring(splitPoint + 1)

  override def toString: String =
    s"${getClass.getSimpleName}(${email.toString})"

  /** Override get(..) to provide custom field accessors */
  override def get(name: String): IdmlValue = name match {
    case "address"  => IdmlValue(addressValue)
    case "name"     => IdmlValue(nameValue)
    case "domain"   => IdmlValue(domainValue)
    case "username" => IdmlValue(usernameValue)
    case _          => MissingField
  }

  /** Split out the username and domain */
  protected val splitPoint = email.getAddress.lastIndexOf('@')

  /** The Email represented as a string */
  val value: String = email.toString

}
