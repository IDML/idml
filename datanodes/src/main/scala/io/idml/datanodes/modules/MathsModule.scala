// scalastyle:off method.name
package io.idml.datanodes.modules

import java.math.{MathContext, RoundingMode}

import io.idml.datanodes.{IDouble, IInt, IString}
import io.idml._
import com.google.common.io.BaseEncoding
import com.google.common.primitives.{Ints, Longs}

import scala.util.Try

/** Adds maths node functions */
trait MathsModule {
  this: IdmlValue =>

  private def doRound(sd: IdmlValue, mode: RoundingMode): IdmlValue = {
    (this.toDoubleOption, sd) match {
      case (Some(d), i: IInt) => IDouble(BigDecimal(d.doubleValue()).underlying().setScale(i.value.toInt, mode).doubleValue)
      case (None, _)          => InvalidCaller
      case (Some(_), _)       => InvalidParameters
    }
  }

  private def doSigRound(sd: IdmlValue, mode: RoundingMode): IdmlValue = {
    (this.toDoubleOption, sd) match {
      case (Some(d), i: IInt) if i.value < 1 => InvalidParameters
      case (Some(d), i: IInt)                => IDouble(BigDecimal(d.doubleValue()).round(new MathContext(i.value.toInt, mode)).doubleValue())
      case (None, _)                         => InvalidCaller
      case (Some(_), _)                      => InvalidParameters
    }
  }

  private def parseHexInner(signed: Boolean): IdmlValue = {
    this match {
      case IString(s) =>
        val array = BaseEncoding.base16().decode(s.toUpperCase)
        Try {
          if (signed)
            IInt(Longs.fromByteArray(array))
          else
            IString(java.lang.Long.toUnsignedString(Longs.fromByteArray(array)))
        }.recoverWith {
            case _: IllegalArgumentException =>
              Try {
                if (signed)
                  IInt(Ints.fromByteArray(array).toLong)
                else
                  IString(java.lang.Integer.toUnsignedString(Ints.fromByteArray(array)))
              }
          }
          .getOrElse(IdmlNull)
      case _ => InvalidCaller
    }
  }

  def parseHex(): IdmlValue         = parseHexInner(true)
  def parseHexUnsigned(): IdmlValue = parseHexInner(false)

  def round(sd: IdmlValue): IdmlValue = doRound(sd, RoundingMode.HALF_UP)
  def round(): IdmlValue              = doRound(IInt(0), RoundingMode.HALF_UP).int()

  def ceil(sd: IdmlValue): IdmlValue = doRound(sd, RoundingMode.CEILING)
  def ceil(): IdmlValue              = doRound(IInt(0), RoundingMode.CEILING).int()

  def floor(sd: IdmlValue): IdmlValue = doRound(sd, RoundingMode.FLOOR)
  def floor(): IdmlValue              = doRound(IInt(0), RoundingMode.FLOOR).int()

  def sigfig(sd: IdmlValue): IdmlValue = doSigRound(sd, RoundingMode.HALF_UP)

  private def extractDouble(v: IdmlValue): Option[Double] = v match {
    case IInt(i)    => Some(i.toDouble)
    case IDouble(d) => Some(d)
    case _          => None
  }

  def log(): IdmlValue = extractDouble(this) match {
    case Some(d) if d <= 0.0 => InvalidCaller
    case Some(d)             => IDouble(java.lang.Math.log(d))
    case None                => InvalidCaller
  }

  def abs(): IdmlValue = this match {
    case IInt(i)    => IInt(java.lang.Math.abs(i))
    case IDouble(d) => IDouble(java.lang.Math.abs(d))
    case _          => InvalidCaller
  }

  def pow(e: IdmlValue): IdmlValue = (extractDouble(this), extractDouble(e)) match {
    case (Some(l), Some(r)) => IDouble(java.lang.Math.pow(l, r))
    case (None, _)          => InvalidCaller
    case (_, None)          => InvalidParameters
  }

  def exp(): IdmlValue = extractDouble(this) match {
    case Some(d) => IDouble(java.lang.Math.exp(d))
    case None    => InvalidCaller
  }

  def sqrt(): IdmlValue = extractDouble(this) match {
    case Some(d) => IDouble(Math.sqrt(d))
    case None    => InvalidCaller
  }

  def /(target: IdmlValue): IdmlValue = {
    this match {
      case i: IInt =>
        target match {
          case ti: IInt if ti.value == 0    => InvalidParameters
          case td: IDouble if td.value == 0 => InvalidParameters
          case ti: IInt                     => IDouble(i.value.toDouble / ti.value)
          case td: IDouble                  => IDouble(i.value / td.value)
          case _                            => InvalidParameters
        }
      case d: IDouble =>
        target match {
          case ti: IInt    => IDouble(d.value / ti.value)
          case td: IDouble => IDouble(d.value / td.value)
          case _           => InvalidParameters
        }
      case _ => InvalidCaller
    }
  }

  def *(target: IdmlValue): IdmlValue = {
    this match {
      case i: IInt =>
        target match {
          case ti: IInt    => IInt(i.value * ti.value)
          case td: IDouble => IDouble(i.value * td.value)
          case _           => InvalidParameters
        }
      case d: IDouble =>
        target match {
          case ti: IInt    => IDouble(d.value * ti.value)
          case td: IDouble => IDouble(d.value * td.value)
          case _           => InvalidParameters
        }
      case _ => InvalidCaller
    }
  }

  def +(target: IdmlValue): IdmlValue = {
    this match {
      case i: IInt =>
        target match {
          case ti: IInt    => IInt(i.value + ti.value)
          case td: IDouble => IDouble(i.value + td.value)
          case _           => InvalidParameters
        }
      case d: IDouble =>
        target match {
          case ti: IInt    => IDouble(d.value + ti.value)
          case td: IDouble => IDouble(d.value + td.value)
          case _           => InvalidParameters
        }
      case s: IdmlString =>
        target match {
          case ts: IdmlString => IString(s.value + ts.value)
          case ti: IInt       => IString(s.value + ti.value.toString)
          case td: IDouble    => IString(s.value + td.value.toString)
          case _              => InvalidParameters
        }
      case _ => InvalidCaller
    }
  }

  def -(target: IdmlValue): IdmlValue = {
    this match {
      case i: IInt =>
        target match {
          case ti: IInt    => IInt(i.value - ti.value)
          case td: IDouble => IDouble(i.value - td.value)
          case _           => InvalidParameters
        }
      case d: IDouble =>
        target match {
          case ti: IInt    => new IDouble(d.value - ti.value)
          case td: IDouble => IDouble(d.value - td.value)
          case _           => InvalidParameters
        }
      case _ => InvalidCaller
    }
  }
}
// scalastyle:on method.name
