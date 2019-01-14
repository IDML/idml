package io.idml.datanodes

import io.idml.datanodes.modules.DateModule
import io.idml.PtolemyString
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter

/** */
case class PDate(dateVal: DateTime, format: DateTimeFormatter = DateModule.DefaultDateFormat) extends PtolemyString with CompositeValue {
  def value: String        = dateVal.toString(format)
  override def int(): PInt = PInt(dateVal.getMillis())
}
