// scalastyle:off import.grouping
package io.idml.datanodes.modules

import java.time.{Instant, LocalDateTime, ZoneId, ZonedDateTime}
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.util.TimeZone

import io.idml
import io.idml.datanodes.{IDate, IDateFormats, IInt, IString}
import io.idml.{BadDateFormat, CastFailed, CastUnsupported, IdmlInt, IdmlNothing, IdmlString, IdmlValue}

import scala.util.{Failure, Success, Try}

object DateModule {

//  DateTimeZone.setDefault(DateTimeZone.UTC)

  /** The default output date format */
  val DefaultDateFormat = IDateFormats.rfc1123

  def millisToDate(n: IdmlValue, pfmt: Any = DefaultDateFormat): IdmlValue = {
    pfmt match {
      case fmtStr: IdmlString =>
        Try(DateTimeFormatter.ofPattern(fmtStr.value)) match {
          case Success(fmt) => millisToDateFMT(n, fmt)
          case _            => BadDateFormat
        }
      case fmt: DateTimeFormatter => millisToDateFMT(n, fmt)
      case _                      => CastUnsupported
    }
  }

  private def millisToDateFMT(n: IdmlValue, fmt: DateTimeFormatter) : IdmlValue = {
    n match {
      case o: IdmlInt => IDate(ZonedDateTime.ofInstant(Instant.ofEpochMilli(o.value), ZoneId.of("UTC")), fmt)
      case o: IdmlString =>
        Try(IDate(ZonedDateTime.ofInstant(Instant.ofEpochMilli(o.value.toLong), ZoneId.of("UTC")), fmt)) match {
          case Success(date) => date
          case Failure(f)    => CastUnsupported
        }
      case _ => CastUnsupported
    }
  }

  def applyTimezone(supplied: IDate, tzStr: String): IdmlValue = {
    IDateFormats.timezone(tzStr) match {
      case Some(tz) =>
        IDate(ZonedDateTime.ofInstant(supplied.dateVal.toInstant, tz))
      case None =>
        CastFailed
    }
  }

  /** */
  def stringToDate(str: String, df: DateTimeFormatter): IdmlValue = {
    Try(ZonedDateTime.from(df.parse(str))) match {
      case Success(date)                        => IDate.create(date).getOrElse(CastFailed)
      case Failure(x: IllegalArgumentException) => CastFailed
    }
  }

  // scalastyle:off return
  /** Convert from a string to a date */
  def stringToDate(str: String): IdmlValue = {
    for (df <- IDateFormats.Formatters) {
      Try{ZonedDateTime.from(df.parse(str))} match {
        case Success(date)                        => return IDate.create(date).getOrElse(CastFailed)
        case Failure(e: IllegalArgumentException) => ()
        case Failure(e)                           => ()
      }
    }
    CastFailed
  }
  // scalastyle:on return

  /** Convert from a unix timestamp to date object */
  def timestampToDate(num: Long): IdmlValue = {
    (num, Math.log10(num) + 1) match {
      case (num, _) if num < 1 => CastFailed
      case (num, digits) if digits < 14 =>
        IDate.create(ZonedDateTime.ofInstant(Instant.ofEpochSecond(num), ZoneId.of("UTC"))).getOrElse(CastFailed)
      case (num, digits) if digits >= 14 =>
        IDate.create(ZonedDateTime.ofInstant(Instant.ofEpochMilli(num), ZoneId.of("UTC"))).getOrElse(CastFailed)
      case _ => CastFailed
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
      Try(DateTimeFormatter.ofPattern(formatStrLike.value)) match {
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

  def rssDate(): IdmlValue = specificDate(DateTimeFormatter.RFC_1123_DATE_TIME)

  def dateToString(df: DateTimeFormatter)(d: IDate) = df.format(d.dateVal)

  def specificDate(df: DateTimeFormatter): IdmlValue = this match {
    case _: IDate | _: IdmlNothing => this
    case n: IdmlString             => stringToDate(n.value, df)
    case n: IdmlInt                => timestampToDate(n.value)
    case _                         => CastUnsupported
  }

  /** The current time in microseconds */
  def microtime(): IdmlValue = IInt(System.currentTimeMillis() * 1000L)

  /** The current date */
  def now(): IdmlValue = IDate(ZonedDateTime.now())

  def millis(): IdmlValue = this match {
    case _: IDate | _: IdmlNothing => this
    case n: IdmlInt                => millisToDate(n)
    case n: IdmlString             => millisToDate(n)
    case _                         => CastUnsupported
  }

  def toEpoch(): IdmlValue = this match {
    case d: IDate => IInt(d.dateVal.toInstant.getEpochSecond)
    case _        => CastUnsupported
  }

  def toEpochMillis(): IdmlValue = this match {
    case d: IDate => IInt(d.dateVal.toInstant.toEpochMilli)
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
