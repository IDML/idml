// scalastyle:off import.grouping
package io.idml.datanodes.modules

import java.util.TimeZone

import io.idml.datanodes.{IDate, IDateFormats, IInt, IString}
import io.idml.{BadDateFormat, CastFailed, CastUnsupported, IdmlInt, IdmlNothing, IdmlString, IdmlValue}
import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

import scala.util.{Failure, Success, Try}

object DateModule {

  DateTimeZone.setDefault(DateTimeZone.UTC)

  /** The default output date format */
  val DefaultDateFormat = IDateFormats.RFC822Printer

  def millisToDate(n: IdmlValue, pfmt: Any = DefaultDateFormat): IdmlValue = {
    pfmt match {
      case fmtStr: IdmlString =>
        Try(DateTimeFormat.forPattern(fmtStr.value)) match {
          case Success(fmt) => millisToDateFMT(n, fmt)
          case _            => BadDateFormat
        }
      case fmt: DateTimeFormatter => millisToDateFMT(n, fmt)
      case _                      => CastUnsupported
    }
  }

  private def millisToDateFMT(n: IdmlValue, fmt: DateTimeFormatter) = {
    n match {
      case o: IdmlInt => IDate(new DateTime(o.value), fmt)
      case o: IdmlString =>
        Try(IDate(new DateTime(o.value.toLong), fmt)) match {
          case Success(date) => date
          case Failure(f)    => CastUnsupported
        }
      case _ => CastUnsupported
    }
  }

  def applyTimezone(supplied: IDate, tzStr: String): IdmlValue = {
    Try(IDateFormats.TimezoneFormatter.withOffsetParsed().parseDateTime(tzStr)) match {
      case Success(tz) =>
        IDate(supplied.dateVal.withZone(tz.getZone))
      case Failure(f) =>
        Try(TimeZone.getTimeZone(tzStr)) match {
          case Success(tz: TimeZone) =>
            IDate(supplied.dateVal.withZone(DateTimeZone.forTimeZone(tz)))
          case Failure(f2) => CastFailed
        }
    }
  }

  /** */
  def stringToDate(str: String, df: DateTimeFormatter): IdmlValue = {
    Try(df.parseDateTime(str)) match {
      case Success(date)                        => new IDate(date)
      case Failure(x: IllegalArgumentException) => CastFailed
      case Failure(e)                           => throw e
    }
  }

  // scalastyle:off return
  /** Convert from a string to a date */
  def stringToDate(str: String): IdmlValue = {
    for (df <- IDateFormats.Formatters) {
      Try(df.withOffsetParsed().parseDateTime(str)) match {
        case Success(date)                        => return new IDate(date)
        case Failure(e: IllegalArgumentException) => ()
        case Failure(e)                           => throw e
      }
    }
    Try(new DateTime(str)) match {
      case Success(date) => return new IDate(date)
      case Failure(e)    => ()
    }
    CastFailed
  }
  // scalastyle:on return

  /** Convert from a unix timestamp to date object */
  def timestampToDate(num: Long): IdmlValue = {
    if (num > 0 && num < Int.MaxValue) {
      new IDate(new DateTime(num * 1000))
    } else {
      CastFailed
    }
  }

}

/** Behaviour for working with dates */
trait DateModule {
  this: IdmlValue =>

  import DateModule.{applyTimezone, millisToDate, stringToDate, timestampToDate}

  /** Try to cast something into a date */
  def date(): IdmlValue = this match {
    case _: IDate | _: IdmlNothing => this
    case n: IdmlString             => stringToDate(n.value)
    case n: IdmlInt                => timestampToDate(n.value)
    case _                         => CastUnsupported
  }

  /**
    * @see java.text.SimpleDateFormat
    * @param formatStr a date format string which will be used to interpret the value of this date
    * @return [[BadDateFormat]] if the date format is invalid, [[CastFailed]] if parsing the date failed for another
    *         reason or a [[io.idml.datanodes.IDate]] representing the results of what was parsed
    */
  def date(formatStr: IdmlValue): IdmlValue = formatStr match {
    case formatStrLike: IdmlString =>
      Try(DateTimeFormat.forPattern(formatStrLike.value)) match {
        case Success(userFormat) =>
          this match {
            case d: IDate       => IString(dateToString(userFormat)(d))
            case _: IdmlNothing => this
            case n: IdmlString  => specificDate(userFormat)
            case n: IdmlInt     => date()
            case _              => CastUnsupported
          }
        case _ => BadDateFormat
      }
    case _ => BadDateFormat
  }

  def rssDate(): IdmlValue = specificDate(IDateFormats.RFC822Printer)

  def dateToString(df: DateTimeFormatter)(d: IDate) = df.print(d.dateVal)

  def specificDate(df: DateTimeFormatter): IdmlValue = this match {
    case _: IDate | _: IdmlNothing => this
    case n: IdmlString             => stringToDate(n.value, df)
    case n: IdmlInt                => timestampToDate(n.value)
    case _                         => CastUnsupported
  }

  /** The current time in microseconds */
  def microtime(): IdmlValue = IInt(System.currentTimeMillis() * 1000L)

  /** The current date */
  def now(): IdmlValue = IDate(new DateTime())

  def millis(): IdmlValue = this match {
    case _: IDate | _: IdmlNothing => this
    case n: IdmlInt                => millisToDate(n)
    case n: IdmlString             => millisToDate(n)
    case _                         => CastUnsupported
  }

  def toEpoch(): IdmlValue = this match {
    case d: IDate => IInt(d.dateVal.getMillis() / 1000L)
    case _        => CastUnsupported
  }

  def toEpochMillis(): IdmlValue = this match {
    case d: IDate => IInt(d.dateVal.getMillis())
    case _        => CastUnsupported
  }

  def timezone(tz: IdmlValue): IdmlValue = tz match {
    case tzStr: IdmlString =>
      this match {
        case date: IDate => applyTimezone(date, tzStr.value)
        case _ =>
          date() match {
            case date: IDate => applyTimezone(date, tzStr.value)
            case _           => CastUnsupported
          }
      }
    case _ => CastUnsupported
  }
}
// scalastyle:on import.grouping
