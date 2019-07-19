package io.idml.datanodes.modules

import javax.mail.internet.InternetAddress

import io.idml.datanodes.{IArray, IEmail}
import io.idml.{CastFailed, CastUnsupported, IdmlArray, IdmlNothing, IdmlString, IdmlValue}

import scala.util.Try

/** Adds email manipulation behaviour to data nodes */
trait EmailModule {
  this: IdmlValue =>

  /** Construct a new Email by parsing a string */
  def email(): IdmlValue = this match {
    case _: IEmail | _: IdmlNothing => this
    case n: IdmlString =>
      Try(new InternetAddress(n.value, true))
        .map(new IEmail(_))
        .getOrElse(CastFailed)
    case a: IdmlArray => new IArray(a.items.map { _.email() })
    case _            => CastUnsupported
  }

}
