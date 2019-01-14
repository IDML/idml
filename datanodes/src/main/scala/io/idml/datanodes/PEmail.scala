package io.idml.datanodes

import javax.mail.internet.InternetAddress

import io.idml.{MissingField, PtolemyString, PtolemyValue}

/** Represents a valid Email */
class PEmail(email: InternetAddress) extends PtolemyString with CompositeValue {

  /** Extract the address component */
  def address: PtolemyValue = PtolemyValue(addressValue)

  /** Extract the name component */
  def name: PtolemyValue = PtolemyValue(nameValue)

  /** Extract the username component */
  def username: PtolemyValue = PtolemyValue(usernameValue)

  /** Extract the domain component */
  def domain: PtolemyValue = PtolemyValue(domainValue)

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
  override def get(name: String): PtolemyValue = name match {
    case "address"  => PtolemyValue(addressValue)
    case "name"     => PtolemyValue(nameValue)
    case "domain"   => PtolemyValue(domainValue)
    case "username" => PtolemyValue(usernameValue)
    case _          => MissingField
  }

  /** Split out the username and domain */
  protected val splitPoint = email.getAddress.lastIndexOf('@')

  /** The Email represented as a string */
  val value: String = email.toString

}
