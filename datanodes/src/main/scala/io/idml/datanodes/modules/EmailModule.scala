package io.idml.datanodes.modules

import javax.mail.internet.InternetAddress

import io.idml.datanodes.{PArray, PEmail}
import io.idml.{CastFailed, CastUnsupported, IdmlArray, IdmlNothing, IdmlString, IdmlValue}

import scala.util.Try

/** Adds email manipulation behaviour to data nodes */
trait EmailModule {
  this: IdmlValue =>

  /** Construct a new Email by parsing a string */
  def email(): IdmlValue = this match {
    case _: PEmail | _: IdmlNothing => this
    case n: IdmlString =>
      Try(new InternetAddress(n.value, true))
        .map(new PEmail(_))
        .getOrElse(CastFailed)
    case a: IdmlArray => new PArray(a.items.map { _.email() })
    case _            => CastUnsupported
  }

}
