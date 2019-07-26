package io.idml.datanodes

import java.time.{Instant, ZoneId, ZonedDateTime}
import java.time.format.DateTimeFormatterBuilder

import io.idml.datanodes.modules.DateModule
import org.scalatest._

/** Test the behaviour of the PDate class */
class IDateTest extends FunSuite with MustMatchers {
//  DateTimeZone.setDefault(DateTimeZone.UTC)

  val UTC        = ZoneId.of("UTC")
  val nowMs      = 1414755054310L
  val now        = ZonedDateTime.ofInstant(Instant.ofEpochMilli(nowMs), UTC)
  val format1    = "yyyy.MM.dd G 'at' HH:mm:ss z"
  val format1Val = "2001.07.04 AD at 12:08:56 PDT"
  val format1Parser =
    new DateTimeFormatterBuilder().appendPattern(format1).toFormatter

  test("parse timestamp")(IInt((nowMs / 1000)).date() must equal(new IDate(now)))

  val pDate = IString(format1Val)

  test("must be able to parse custom format")(
    pDate.date(new IString(format1)).asInstanceOf[IDate].dateVal
      must equal(new IDate(ZonedDateTime.from(format1Parser.parse(format1Val)), format1Parser).dateVal))

  test("Any custom formatted date is correctly parsed and translated to GMT") {
    //formattedOutput = formattedInput.date("yyyy.MM.dd G 'at' HH:mm:ss z")
    println(pDate.date(new IString(format1)).asInstanceOf[IDate].value)
  }

  test("Correctly interpret millisecond timestamps from a long") {
    val now    = 1414755054310L
    val parsed = IInt(now)
    val actual = parsed.millis()
    assert(classOf[IDate] isAssignableFrom actual.getClass)
    actual.asInstanceOf[IDate].dateVal.toInstant.toEpochMilli must equal(now)
  }

  test("Correctly interpret millisecond timestamps from a string") {
    val nowLong = 1414755054310L
    val parsed  = IString(nowLong.toString)
    val actual  = parsed.millis()
    //make sure the parsed value is actually a PString
    assert(classOf[IString] isAssignableFrom parsed.getClass)
    //Once we have our PDate, verify we got the right object out and the underlying date object has the same timestamp
    assert(classOf[IDate] isAssignableFrom actual.getClass)
    actual.asInstanceOf[IDate].dateVal must equal(ZonedDateTime.ofInstant(Instant.ofEpochMilli(nowLong), UTC))
  }

  test("parse millis timestamp")(IString(nowMs.toString).millis() must equal(new IDate(now, DateModule.DefaultDateFormat)))

  test("Parse timezone offsets") {
    import io.idml.datanodes.modules.DateModule._
    val now          = IDate(ZonedDateTime.ofInstant(Instant.ofEpochMilli(1414755054310L), UTC))
    val nowMinus8Hrs = now.dateVal.minusHours(8)
    val nowPlus8Hrs  = now.dateVal.plusHours(8)

    Array("-0800", "PST", "-08", "-08:00") foreach { tz =>
      val date = applyTimezone(now, tz).asInstanceOf[IDate]
      date.dateVal.toInstant.toEpochMilli must equal(now.dateVal.toInstant.toEpochMilli)
      date.dateVal.getZone must not equal now.dateVal.getZone
    }

    Array("+0800", "+08", "+0800", "+08:00") foreach { tz =>
      val date = applyTimezone(now, tz).asInstanceOf[IDate]
      date.dateVal.toInstant.toEpochMilli must equal(now.dateVal.toInstant.toEpochMilli)
      date.dateVal.getZone must not equal now.dateVal.getZone
    }
  }

  test("Localize with timezone offsets") {
    val nowLong = 1414755054310L
    val nowdt   = ZonedDateTime.ofInstant(Instant.ofEpochMilli(nowLong), UTC)
    val parsed  = IString(nowLong.toString)
    val actual  = parsed.millis().timezone(IString("-0800"))
    assert(classOf[IDate] isAssignableFrom actual.getClass)
    actual.asInstanceOf[IDate].dateVal.getZone.toString must equal("-08:00")
  }

  test("Can parse Twitter-style dates") {
    val d = ZonedDateTime.ofInstant(Instant.ofEpochMilli(1323833748000L), UTC)
    val x = IDate(d)
    val y = IString("Wed Dec 14 03:35:48 +0000 2011").date()
    assert(IString("Wed Dec 14 03:35:48 +0000 2011").date() === IDate(ZonedDateTime.ofInstant(Instant.ofEpochMilli(1323833748000L), UTC)))
  }

  test("Can parse Twitter-style dates and convert them to an integer") {
    val d = ZonedDateTime.ofInstant(Instant.ofEpochMilli(1323833748000L), UTC)
    val x = IDate(d)
    val y = IString("Wed Dec 14 03:35:48 +0000 2011").date()
    assert(IString("Wed Dec 14 03:35:48 +0000 2011").date().int() === IInt(1323833748000L))
  }

  test("Can format a Date back out in a custom format") {
    val d = IDate(now)
    val o = d.date(IString("YYYY-MM-dd"))
    assert(o.toStringValue === ("2014-10-31"))
  }
}
