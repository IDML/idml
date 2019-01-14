package io.idml.datanodes.modules

import io.idml.datanodes.{PArray, PBool, PString}
import io.idml.datanodes.regex.PRegexFactory
import io.idml.{InvalidCaller, InvalidParameters, PtolemyValue}

/** Methods for working with regular expressions */
trait RegexModule {
  this: PtolemyValue =>

  def matches(regex: PtolemyValue): PtolemyValue = {
    (this, regex) match {
      case (PString(s), PString(r)) =>
        PArray(
          PRegexFactory
            .getRegex(r)
            .matches(s)
            .map { inner =>
              PArray(inner.map(PString.apply).toBuffer[PtolemyValue])
            }
            .toBuffer[PtolemyValue])
      case (PString(_), _) => InvalidParameters
      case _               => InvalidCaller
    }
  }

  // scalastyle:off method.name
  def `match`(regex: PtolemyValue): PtolemyValue = {
    this match {
      case t: PString =>
        regex match {
          case s: PString =>
            PArray(
              PRegexFactory
                .getRegex(s.value)
                .`match`(t.value)
                .map { PString }
                .toBuffer[PtolemyValue])
          case _ => InvalidParameters
        }
      case _ => InvalidCaller
    }
  }
  // scalastyle:on method.name

  def split(delim: PtolemyValue): PtolemyValue = {
    this match {
      case t: PString =>
        delim match {
          case s: PString =>
            PArray(
              PRegexFactory
                .getRegex(s.value)
                .split(t.value)
                .map { PString }
                .toBuffer[PtolemyValue])
          case _ => InvalidParameters
        }
      case _ => InvalidCaller
    }
  }

  def isMatch(regex: PtolemyValue): PtolemyValue = {
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

  def replace(regex: PtolemyValue, replacement: PtolemyValue): PtolemyValue = {
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
