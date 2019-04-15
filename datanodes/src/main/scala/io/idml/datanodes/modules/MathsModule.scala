// scalastyle:off method.name
package io.idml.datanodes.modules

import java.math.{MathContext, RoundingMode}

import io.idml.datanodes.{PDouble, PInt, PString}
import io.idml._
import com.google.common.io.BaseEncoding
import com.google.common.primitives.{Ints, Longs}

import scala.util.Try

/** Adds maths node functions */
trait MathsModule {
  this: PtolemyValue =>

  private def doRound(sd: PtolemyValue, mode: RoundingMode): PtolemyValue = {
    (this.toDoubleOption, sd) match {
      case (Some(d), i: PInt) => PDouble(BigDecimal(d.doubleValue()).underlying().setScale(i.value.toInt, mode).doubleValue)
      case (None, _)          => InvalidCaller
      case (Some(_), _)       => InvalidParameters
    }
  }

  private def doSigRound(sd: PtolemyValue, mode: RoundingMode): PtolemyValue = {
    (this.toDoubleOption, sd) match {
      case (Some(d), i: PInt) if i.value < 1 => InvalidParameters
      case (Some(d), i: PInt)                => PDouble(BigDecimal(d.doubleValue()).round(new MathContext(i.value.toInt, mode)).doubleValue())
      case (None, _)                         => InvalidCaller
      case (Some(_), _)                      => InvalidParameters
    }
  }

  private def parseHexInner(signed: Boolean): PtolemyValue = {
    this match {
      case PString(s) =>
        val array = BaseEncoding.base16().decode(s.toUpperCase)
        Try {
          if (signed)
            PInt(Longs.fromByteArray(array))
          else
            PString(java.lang.Long.toUnsignedString(Longs.fromByteArray(array)))
        }.recoverWith {
            case _: IllegalArgumentException =>
              Try {
                if (signed)
                  PInt(Ints.fromByteArray(array).toLong)
                else
                  PString(java.lang.Integer.toUnsignedString(Ints.fromByteArray(array)))
              }
          }
          .getOrElse(PtolemyNull)
      case _ => InvalidCaller
    }
  }

  def parseHex(): PtolemyValue         = parseHexInner(true)
  def parseHexUnsigned(): PtolemyValue = parseHexInner(false)

  def round(sd: PtolemyValue): PtolemyValue = doRound(sd, RoundingMode.HALF_UP)
  def round(): PtolemyValue                 = doRound(PInt(0), RoundingMode.HALF_UP).int()

  def ceil(sd: PtolemyValue): PtolemyValue = doRound(sd, RoundingMode.CEILING)
  def ceil(): PtolemyValue                 = doRound(PInt(0), RoundingMode.CEILING).int()

  def floor(sd: PtolemyValue): PtolemyValue = doRound(sd, RoundingMode.FLOOR)
  def floor(): PtolemyValue                 = doRound(PInt(0), RoundingMode.FLOOR).int()

  def sigfig(sd: PtolemyValue): PtolemyValue = doSigRound(sd, RoundingMode.HALF_UP)

  private def extractDouble(v: PtolemyValue): Option[Double] = v match {
    case PInt(i)    => Some(i.toDouble)
    case PDouble(d) => Some(d)
    case _          => None
  }

  def log(): PtolemyValue = extractDouble(this) match {
    case Some(0.0)         => InvalidCaller
    case Some(d) if d <= 0 => InvalidCaller
    case Some(d)           => PDouble(java.lang.Math.log(d))
    case None              => InvalidCaller
  }

  def abs(): PtolemyValue = extractDouble(this) match {
    case Some(d) => PDouble(java.lang.Math.abs(d))
    case None    => InvalidCaller
  }

  def pow(e: PtolemyValue): PtolemyValue = (extractDouble(this), extractDouble(e)) match {
    case (Some(l), Some(r)) => PDouble(java.lang.Math.pow(l, r))
    case (None, _)          => InvalidCaller
    case (_, None)          => InvalidParameters
  }

  def exp(): PtolemyValue = extractDouble(this) match {
    case Some(d) => PDouble(java.lang.Math.exp(d))
    case None    => InvalidCaller
  }

  def sqrt(): PtolemyValue = extractDouble(this) match {
    case Some(d) => PDouble(Math.sqrt(d))
    case None    => InvalidCaller
  }

  def /(target: PtolemyValue): PtolemyValue = {
    this match {
      case i: PInt =>
        target match {
          case ti: PInt if ti.value == 0    => InvalidParameters
          case td: PDouble if td.value == 0 => InvalidParameters
          case ti: PInt                     => PDouble(i.value.toDouble / ti.value)
          case td: PDouble                  => PDouble(i.value / td.value)
          case _                            => InvalidParameters
        }
      case d: PDouble =>
        target match {
          case ti: PInt    => PDouble(d.value / ti.value)
          case td: PDouble => PDouble(d.value / td.value)
          case _           => InvalidParameters
        }
      case _ => InvalidCaller
    }
  }

  def *(target: PtolemyValue): PtolemyValue = {
    this match {
      case i: PInt =>
        target match {
          case ti: PInt    => PInt(i.value * ti.value)
          case td: PDouble => PDouble(i.value * td.value)
          case _           => InvalidParameters
        }
      case d: PDouble =>
        target match {
          case ti: PInt    => PDouble(d.value * ti.value)
          case td: PDouble => PDouble(d.value * td.value)
          case _           => InvalidParameters
        }
      case _ => InvalidCaller
    }
  }

  def +(target: PtolemyValue): PtolemyValue = {
    this match {
      case i: PInt =>
        target match {
          case ti: PInt    => PInt(i.value + ti.value)
          case td: PDouble => PDouble(i.value + td.value)
          case _           => InvalidParameters
        }
      case d: PDouble =>
        target match {
          case ti: PInt    => PDouble(d.value + ti.value)
          case td: PDouble => PDouble(d.value + td.value)
          case _           => InvalidParameters
        }
      case s: PtolemyString =>
        target match {
          case ts: PtolemyString => PString(s.value + ts.value)
          case ti: PInt          => PString(s.value + ti.value.toString)
          case td: PDouble       => PString(s.value + td.value.toString)
          case _                 => InvalidParameters
        }
      case _ => InvalidCaller
    }
  }

  def -(target: PtolemyValue): PtolemyValue = {
    this match {
      case i: PInt =>
        target match {
          case ti: PInt    => PInt(i.value - ti.value)
          case td: PDouble => PDouble(i.value - td.value)
          case _           => InvalidParameters
        }
      case d: PDouble =>
        target match {
          case ti: PInt    => new PDouble(d.value - ti.value)
          case td: PDouble => PDouble(d.value - td.value)
          case _           => InvalidParameters
        }
      case _ => InvalidCaller
    }
  }
}
// scalastyle:on method.name
