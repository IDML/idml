package io.idml.datanodes

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import io.idml.datanodes.modules.DateModule
import io.idml.IdmlString

import scala.util.Try

/** */
case class IDate(dateVal: ZonedDateTime, format: DateTimeFormatter = DateModule.DefaultDateFormat) extends IdmlString with CompositeValue {
  def value: String        = Try { format.format(dateVal) }.getOrElse("Unknown")
  override def int(): IInt = IInt(dateVal.toInstant.toEpochMilli)
}


object IDate {
  def create(dateVal: ZonedDateTime, format: DateTimeFormatter = DateModule.DefaultDateFormat): Option[IDate] =
    Try {
      val d = new IDate(dateVal, format)
      d.value
      d
    }.toOption
}