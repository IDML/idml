package io.idml.datanodes.modules

import io.idml.datanodes.{IArray, IBool, IString}
import io.idml.datanodes.regex.PRegexFactory
import io.idml.{IdmlValue, InvalidCaller, InvalidParameters}

/** Methods for working with regular expressions */
trait RegexModule {
  this: IdmlValue =>

  def matches(regex: IdmlValue): IdmlValue = {
    (this, regex) match {
      case (IString(s), IString(r)) =>
        IArray(
          PRegexFactory
            .getRegex(r)
            .matches(s)
            .map { inner =>
              IArray(inner.map(IString.apply).toBuffer[IdmlValue])
            }
            .toBuffer[IdmlValue])
      case (IString(_), _) => InvalidParameters
      case _               => InvalidCaller
    }
  }

  // scalastyle:off method.name
  def `match`(regex: IdmlValue): IdmlValue = {
    this match {
      case t: IString =>
        regex match {
          case s: IString =>
            IArray(
              PRegexFactory
                .getRegex(s.value)
                .`match`(t.value)
                .map { IString }
                .toBuffer[IdmlValue])
          case _ => InvalidParameters
        }
      case _ => InvalidCaller
    }
  }
  // scalastyle:on method.name

  def split(delim: IdmlValue): IdmlValue = {
    this match {
      case t: IString =>
        delim match {
          case s: IString =>
            IArray(
              PRegexFactory
                .getRegex(s.value)
                .split(t.value)
                .map { IString }
                .toBuffer[IdmlValue])
          case _ => InvalidParameters
        }
      case _ => InvalidCaller
    }
  }

  def isMatch(regex: IdmlValue): IdmlValue = {
    this match {
      case t: IString =>
        regex match {
          case s: IString =>
            IBool(PRegexFactory.getRegex(s.value).isMatch(t.value))
          case _ => InvalidParameters
        }
      case _ => InvalidCaller
    }
  }

  def replace(regex: IdmlValue, replacement: IdmlValue): IdmlValue = {
    this match {
      case t: IString =>
        replacement match {
          case rep: IString =>
            regex match {
              case s: IString =>
                IString(PRegexFactory.getRegex(s.value).replace(t.value, rep.value))
              case _ => InvalidParameters
            }
          case _ => InvalidParameters
        }
      case _ => InvalidCaller
    }
  }

}
