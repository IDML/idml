package io.idml.datanodes.modules

import javax.mail.internet.InternetAddress

import io.idml.datanodes.{PArray, PEmail}
import io.idml.{CastFailed, CastUnsupported, PtolemyArray, PtolemyNothing, PtolemyString, PtolemyValue}

import scala.util.Try

/** Adds email manipulation behaviour to data nodes */
trait EmailModule {
  this: PtolemyValue =>

  /** Construct a new Email by parsing a string */
  def email(): PtolemyValue = this match {
    case _: PEmail | _: PtolemyNothing => this
    case n: PtolemyString =>
      Try(new InternetAddress(n.value, true))
        .map(new PEmail(_))
        .getOrElse(CastFailed)
    case a: PtolemyArray => new PArray(a.items.map { _.email() })
    case _               => CastUnsupported
  }

}
