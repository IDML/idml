package io.idml.datanodes

import io.idml.datanodes.modules.DateModule
import io.idml.IdmlString
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter

/** */
case class IDate(dateVal: DateTime, format: DateTimeFormatter = DateModule.DefaultDateFormat) extends IdmlString with CompositeValue {
  def value: String        = dateVal.toString(format)
  override def int(): IInt = IInt(dateVal.getMillis())
}
