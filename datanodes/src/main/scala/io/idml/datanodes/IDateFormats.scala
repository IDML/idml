package io.idml.datanodes

import org.joda.time.format.{
  DateTimeFormat,
  DateTimeFormatterBuilder,
  DateTimeParser,
  ISODateTimeFormat
}

/** Date formats */
object IDateFormats {
  val minFields = 2
  val maxFields = 4
  val minDigits = 2
  val maxDigits = 4
  val space     = " "

  private val RFC822Day      = new DateTimeFormatterBuilder()
    .appendDayOfWeekShortText()
    .appendLiteral(",")
    .toParser
  private val RFC822Seconds  = new DateTimeFormatterBuilder()
    .appendLiteral(":")
    .appendSecondOfMinute(minDigits)
    .toParser
  private val RFC822Tz       = new DateTimeFormatterBuilder()
    .appendLiteral(space)
    .appendTimeZoneId()
    .toParser
  private val RFC822TzOffset = new DateTimeFormatterBuilder()
    .appendLiteral(space)
    .appendTimeZoneOffset("+0000", false, minFields, maxFields)
    .toParser
  val RFC822                 = new DateTimeFormatterBuilder()
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
  val RFC822Printer          = DateTimeFormat.forPattern("E, dd MMM y HH:mm:ss Z")
  val TwitterDate            = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss Z yyyy")

  val Formatters = List(
    TwitterDate,
    RFC822,
    DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"),
    DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ssZZ"),
    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ"),
    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss"),
    ISODateTimeFormat.basicDateTime(),
    ISODateTimeFormat.basicTTime(),
    ISODateTimeFormat.basicDateTimeNoMillis(),
    ISODateTimeFormat.date()
  )

  private val timezones = List(
    new DateTimeFormatterBuilder().appendTimeZoneId().toParser,
    new DateTimeFormatterBuilder().appendTimeZoneShortName().toParser,
    new DateTimeFormatterBuilder().appendTimeZoneName().toParser,
    new DateTimeFormatterBuilder()
      .appendTimeZoneOffset("0000", false, minFields, maxFields)
      .toParser,
    new DateTimeFormatterBuilder()
      .appendTimeZoneOffset("00:00", true, minFields, maxFields)
      .toParser
  ).toArray[DateTimeParser]

  // scalastyle:off null
  val TimezoneFormatter = new DateTimeFormatterBuilder()
    .append(null, timezones)
    .toFormatter
  // scalastyle:on null
}
