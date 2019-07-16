package io.idml.datanodes

import io.idml.datanodes.modules.DateModule
import org.joda.time.format.DateTimeFormatterBuilder
import org.joda.time.{DateTime, DateTimeZone, Hours}
import org.scalatest._

/** Test the behaviour of the PDate class */
class PDateTest extends FunSuite with MustMatchers {
  DateTimeZone.setDefault(DateTimeZone.UTC)

  val nowMs      = 1414755054310L
  val now        = new DateTime(nowMs)
  val format1    = "yyyy.MM.dd G 'at' HH:mm:ss z"
  val format1Val = "2001.07.04 AD at 12:08:56 PDT"
  val format1Parser =
    new DateTimeFormatterBuilder().appendPattern(format1).toFormatter

  test("parse timestamp")(PInt((nowMs / 1000)).date() must equal(new PDate(now)))

  val pDate = PString(format1Val)

  test("must be able to parse custom format")(
    pDate.date(new PString(format1)).asInstanceOf[PDate].dateVal
      must equal(new PDate(format1Parser.parseDateTime(format1Val), format1Parser).dateVal))

  test("Any custom formatted date is correctly parsed and translated to GMT") {
    //formattedOutput = formattedInput.date("yyyy.MM.dd G 'at' HH:mm:ss z")
    println(pDate.date(new PString(format1)).asInstanceOf[PDate].value)
  }

  test("Correctly interpret millisecond timestamps from a long") {
    val now    = 1414755054310L
    val parsed = PInt(now)
    val actual = parsed.millis()
    assert(classOf[PDate] isAssignableFrom actual.getClass)
    actual.asInstanceOf[PDate].dateVal.isEqual(now) must equal(true)
  }

  test("Correctly interpret millisecond timestamps from a string") {
    val nowLong = 1414755054310L
    val parsed = PString(nowLong.toString)
    val actual  = parsed.millis()
    //make sure the parsed value is actually a PString
    assert(classOf[PString] isAssignableFrom parsed.getClass)
    //Once we have our PDate, verify we got the right object out and the underlying date object has the same timestamp
    assert(classOf[PDate] isAssignableFrom actual.getClass)
    actual.asInstanceOf[PDate].dateVal.equals(new DateTime(nowLong)) must equal(true)
  }

  test("parse millis timestamp")(PString(nowMs.toString).millis() must equal(new PDate(now, DateModule.DefaultDateFormat)))

  test("Parse timezone offsets") {
    import io.idml.datanodes.modules.DateModule._
    val now          = PDate(new DateTime(1414755054310L))
    val nowMinus8Hrs = now.dateVal.minus(Hours.EIGHT)
    val nowPlus8Hrs  = now.dateVal.plus(Hours.EIGHT)

    Array("-0800", "PST", "-08", "-08:00") foreach { tz =>
      val date = applyTimezone(now, tz).asInstanceOf[PDate]
      date.dateVal.getMillis must equal(now.dateVal.getMillis)
      date.dateVal.getZone must not equal now.dateVal.getZone
    }

    Array("+0800", "+08", "+0800", "+08:00") foreach { tz =>
      val date = applyTimezone(now, tz).asInstanceOf[PDate]
      date.dateVal.getMillis must equal(now.dateVal.getMillis)
      date.dateVal.getZone must not equal now.dateVal.getZone
    }
  }

  test("Localize with timezone offsets") {
    val nowLong = 1414755054310L
    val nowdt   = new DateTime(nowLong)
    val parsed  = PString(nowLong.toString)
    val actual  = parsed.millis().timezone(PString("-0800"))
    assert(classOf[PDate] isAssignableFrom actual.getClass)
    actual.asInstanceOf[PDate].dateVal.getZone.toString must equal("-08:00")
  }

  test("Can parse Twitter-style dates") {
    val d = new DateTime(1323833748000L)
    val x = PDate(new DateTime(d))
    val y = PString("Wed Dec 14 03:35:48 +0000 2011").date()
    assert(PString("Wed Dec 14 03:35:48 +0000 2011").date() === PDate(new DateTime(1323833748000L)))
  }

  test("Can parse Twitter-style dates and convert them to an integer") {
    val d = new DateTime(1323833748000L)
    val x = PDate(new DateTime(d))
    val y = PString("Wed Dec 14 03:35:48 +0000 2011").date()
    assert(PString("Wed Dec 14 03:35:48 +0000 2011").date().int() === PInt(1323833748000L))
  }

  test("Can format a Date back out in a custom format") {
    val d = PDate(new DateTime(now))
    val o = d.date(PString("YYYY-MM-dd"))
    assert(o.toStringValue === ("2014-10-31"))
  }
}
