package io.idml.datanodes.modules

import io.idml.datanodes.{PArray, PBool, PString}
import io.idml.datanodes.regex.PRegexFactory
import io.idml.{IdmlValue, InvalidCaller, InvalidParameters}

/** Methods for working with regular expressions */
trait RegexModule {
  this: IdmlValue =>

  def matches(regex: IdmlValue): IdmlValue = {
    (this, regex) match {
      case (PString(s), PString(r)) =>
        PArray(
          PRegexFactory
            .getRegex(r)
            .matches(s)
            .map { inner =>
              PArray(inner.map(PString.apply).toBuffer[IdmlValue])
            }
            .toBuffer[IdmlValue])
      case (PString(_), _) => InvalidParameters
      case _               => InvalidCaller
    }
  }

  // scalastyle:off method.name
  def `match`(regex: IdmlValue): IdmlValue = {
    this match {
      case t: PString =>
        regex match {
          case s: PString =>
            PArray(
              PRegexFactory
                .getRegex(s.value)
                .`match`(t.value)
                .map { PString }
                .toBuffer[IdmlValue])
          case _ => InvalidParameters
        }
      case _ => InvalidCaller
    }
  }
  // scalastyle:on method.name

  def split(delim: IdmlValue): IdmlValue = {
    this match {
      case t: PString =>
        delim match {
          case s: PString =>
            PArray(
              PRegexFactory
                .getRegex(s.value)
                .split(t.value)
                .map { PString }
                .toBuffer[IdmlValue])
          case _ => InvalidParameters
        }
      case _ => InvalidCaller
    }
  }

  def isMatch(regex: IdmlValue): IdmlValue = {
    this match {
      case t: PString =>
        regex match {
          case s: PString =>
            PBool(PRegexFactory.getRegex(s.value).isMatch(t.value))
          case _ => InvalidParameters
        }
      case _ => InvalidCaller
    }
  }

  def replace(regex: IdmlValue, replacement: IdmlValue): IdmlValue = {
    this match {
      case t: PString =>
        replacement match {
          case rep: PString =>
            regex match {
              case s: PString =>
                PString(PRegexFactory.getRegex(s.value).replace(t.value, rep.value))
              case _ => InvalidParameters
            }
          case _ => InvalidParameters
        }
      case _ => InvalidCaller
    }
  }

}
