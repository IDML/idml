package io.idml.datanodes

import java.time.{ZoneId, ZoneOffset}
import java.time.format._
import java.time.temporal.TemporalAccessor

import scala.util.Try
import cats.implicits._

/** Date formats */
object IDateFormats {
  val minFields = 2
  val maxFields = 4
  val minDigits = 2
  val maxDigits = 4
  val space     = " "

  val rfc1123: DateTimeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME

  /*
  private val RFC822Day = new DateTimeFormatterBuilder()
    .appendText(DAY_OF_WEEK, )
    .appendDayOfWeekShortText()
    .appendLiteral(",")
    .toParser
  private val RFC822Seconds = new DateTimeFormatterBuilder()
    .appendLiteral(":")
    .appendSecondOfMinute(minDigits)
    .toParser
  private val RFC822Tz = new DateTimeFormatterBuilder()
    .appendLiteral(space)
    .appendTimeZoneId()
    .toParser
  private val RFC822TzOffset = new DateTimeFormatterBuilder()
    .appendLiteral(space)
    .appendTimeZoneOffset("+0000", false, minFields, maxFields)
    .toParser
  val RFC822 = new DateTimeFormatterBuilder()
    .appendOptional(RFC822Day)
    .appendOptional(DateTimeFormat.forPattern(space).getParser)
    .appendDayOfMonth(minDigits)
    .appendLiteral(space)
    .appendMonthOfYearShortText()
    .appendLiteral(space)
    .appendYear(minDigits, maxDigits)
    .appendLiteral(space)
    .appendPattern("HH:mm")
    .appendOptional(RFC822Seconds)
    .appendOptional(RFC822Tz)
    .appendOptional(RFC822TzOffset)
    .toFormatter
  val RFC822Printer = DateTimeFormat.forPattern("E, dd MMM y HH:mm:ss Z")
   */
  val TwitterDate   = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy")
  val Formatters = List(
    TwitterDate,
    DateTimeFormatter.RFC_1123_DATE_TIME,
//    RFC822,
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssZZ"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZZ"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
    DateTimeFormatter.ISO_DATE_TIME,
    DateTimeFormatter.ISO_TIME,
    DateTimeFormatter.ISO_DATE
  )

  val timezoneParsers: List[DateTimeFormatter] = List(
    new DateTimeFormatterBuilder().appendZoneOrOffsetId().toFormatter,
    new DateTimeFormatterBuilder()
      .appendOffset("+HHMM", "0000")
      .toFormatter,
    new DateTimeFormatterBuilder()
      .appendOffset("+HH:MM", "00:00")
      .toFormatter
  )

  object HasZoneId {
    def unapply(t: TemporalAccessor): Option[ZoneId] = Try { ZoneId.from(t) }.toOption
  }
  object HasZoneOffset {
    def unapply(t: TemporalAccessor): Option[ZoneOffset] = Try { ZoneOffset.from(t) }.toOption
  }
  object IsShortCode {
    def unapply(s: String): Option[ZoneId] = Try { ZoneId.of(s, ZoneId.SHORT_IDS) }.toOption
  }

  def timezone(s: String): Option[ZoneId] =
    timezoneParsers
      .flatMap(t => Try { t.parse(s) }.toOption)
      .collectFirst {
        case HasZoneId(id) => id
        case HasZoneOffset(offset) => offset
      }.recoverWith {
      case _ => IsShortCode.unapply(s)
    }

}
