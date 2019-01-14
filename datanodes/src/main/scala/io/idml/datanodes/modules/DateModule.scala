// scalastyle:off import.grouping
package io.idml.datanodes.modules

import java.util.TimeZone

import io.idml.datanodes.{PDate, PDateFormats, PInt, PString}
import io.idml.{BadDateFormat, CastFailed, CastUnsupported, PtolemyInt, PtolemyNothing, PtolemyString, PtolemyValue}
import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

import scala.util.{Failure, Success, Try}

object DateModule {

  DateTimeZone.setDefault(DateTimeZone.UTC)

  /** The default output date format */
  val DefaultDateFormat = PDateFormats.RFC822Printer

  def millisToDate(n: PtolemyValue, pfmt: Any = DefaultDateFormat): PtolemyValue = {
    pfmt match {
      case fmtStr: PtolemyString =>
        Try(DateTimeFormat.forPattern(fmtStr.value)) match {
          case Success(fmt) => millisToDateFMT(n, fmt)
          case _            => BadDateFormat
        }
      case fmt: DateTimeFormatter => millisToDateFMT(n, fmt)
      case _                      => CastUnsupported
    }
  }

  private def millisToDateFMT(n: PtolemyValue, fmt: DateTimeFormatter) = {
    n match {
      case o: PtolemyInt => PDate(new DateTime(o.value), fmt)
      case o: PtolemyString =>
        Try(PDate(new DateTime(o.value.toLong), fmt)) match {
          case Success(date) => date
          case Failure(f)    => CastUnsupported
        }
      case _ => CastUnsupported
    }
  }

  def applyTimezone(supplied: PDate, tzStr: String): PtolemyValue = {
    Try(PDateFormats.TimezoneFormatter.withOffsetParsed().parseDateTime(tzStr)) match {
      case Success(tz) =>
        PDate(supplied.dateVal.withZone(tz.getZone))
      case Failure(f) =>
        Try(TimeZone.getTimeZone(tzStr)) match {
          case Success(tz: TimeZone) =>
            PDate(supplied.dateVal.withZone(DateTimeZone.forTimeZone(tz)))
          case Failure(f2) => CastFailed
        }
    }
  }

  /** */
  def stringToDate(str: String, df: DateTimeFormatter): PtolemyValue = {
    Try(df.parseDateTime(str)) match {
      case Success(date)                        => new PDate(date)
      case Failure(x: IllegalArgumentException) => CastFailed
      case Failure(e)                           => throw e
    }
  }

  // scalastyle:off return
  /** Convert from a string to a date */
  def stringToDate(str: String): PtolemyValue = {
    for (df <- PDateFormats.Formatters) {
      Try(df.withOffsetParsed().parseDateTime(str)) match {
        case Success(date)                        => return new PDate(date)
        case Failure(e: IllegalArgumentException) => ()
        case Failure(e)                           => throw e
      }
    }
    Try(new DateTime(str)) match {
      case Success(date) => return new PDate(date)
      case Failure(e)    => ()
    }
    CastFailed
  }
  // scalastyle:on return

  /** Convert from a unix timestamp to date object */
  def timestampToDate(num: Long): PtolemyValue = {
    if (num > 0 && num < Int.MaxValue) {
      new PDate(new DateTime(num * 1000))
    } else {
      CastFailed
    }
  }

}

/** Behaviour for working with dates */
trait DateModule {
  this: PtolemyValue =>

  import DateModule.{applyTimezone, millisToDate, stringToDate, timestampToDate}

  /** Try to cast something into a date */
  def date(): PtolemyValue = this match {
    case _: PDate | _: PtolemyNothing => this
    case n: PtolemyString             => stringToDate(n.value)
    case n: PtolemyInt                => timestampToDate(n.value)
    case _                            => CastUnsupported
  }

  /**
    * @see java.text.SimpleDateFormat
    * @param formatStr a date format string which will be used to interpret the value of this date
    * @return [[BadDateFormat]] if the date format is invalid, [[CastFailed]] if parsing the date failed for another
    *         reason or a [[io.idml.datanodes.PDate]] representing the results of what was parsed
    */
  def date(formatStr: PtolemyValue): PtolemyValue = formatStr match {
    case formatStrLike: PtolemyString =>
      Try(DateTimeFormat.forPattern(formatStrLike.value)) match {
        case Success(userFormat) =>
          this match {
            case d: PDate          => PString(dateToString(userFormat)(d))
            case _: PtolemyNothing => this
            case n: PtolemyString  => specificDate(userFormat)
            case n: PtolemyInt     => date()
            case _                 => CastUnsupported
          }
        case _ => BadDateFormat
      }
    case _ => BadDateFormat
  }

  def rssDate(): PtolemyValue = specificDate(PDateFormats.RFC822Printer)

  def dateToString(df: DateTimeFormatter)(d: PDate) = df.print(d.dateVal)

  def specificDate(df: DateTimeFormatter): PtolemyValue = this match {
    case _: PDate | _: PtolemyNothing => this
    case n: PtolemyString             => stringToDate(n.value, df)
    case n: PtolemyInt                => timestampToDate(n.value)
    case _                            => CastUnsupported
  }

  /** The current time in microseconds */
  def microtime(): PtolemyValue = PInt(System.currentTimeMillis() * 1000L)

  /** The current date */
  def now(): PtolemyValue = PDate(new DateTime())

  def millis(): PtolemyValue = this match {
    case _: PDate | _: PtolemyNothing => this
    case n: PtolemyInt                => millisToDate(n)
    case n: PtolemyString             => millisToDate(n)
    case _                            => CastUnsupported
  }

  def toEpoch(): PtolemyValue = this match {
    case d: PDate => PInt(d.dateVal.getMillis() / 1000L)
    case _        => CastUnsupported
  }

  def toEpochMillis(): PtolemyValue = this match {
    case d: PDate => PInt(d.dateVal.getMillis())
    case _        => CastUnsupported
  }

  def timezone(tz: PtolemyValue): PtolemyValue = tz match {
    case tzStr: PtolemyString =>
      this match {
        case date: PDate => applyTimezone(date, tzStr.value)
        case _ =>
          date() match {
            case date: PDate => applyTimezone(date, tzStr.value)
            case _           => CastUnsupported
          }
      }
    case _ => CastUnsupported
  }
}
// scalastyle:on import.grouping
