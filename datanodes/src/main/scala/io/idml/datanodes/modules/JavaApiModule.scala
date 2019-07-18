package io.idml.datanodes.modules

import java.util.Optional
import java.lang._

import io.idml.IdmlValue
import io.idml.datanodes.{IArray, IBool, IDouble, IInt, IObject, IString}

import scala.collection.JavaConverters._

trait JavaApiModule {
  this: IdmlValue =>

  def asBoolean(): Optional[Boolean] = this match {
    case b: IBool => Optional.of(b.value.asInstanceOf[java.lang.Boolean])
    case _        => Optional.empty[java.lang.Boolean]
  }

  def asString(): Optional[java.lang.String] = this match {
    case s: IString => Optional.of(s.value)
    case _          => Optional.empty[String]
  }

  def asLong(): Optional[java.lang.Long] = this match {
    case l: IInt => Optional.of(l.value)
    case _       => Optional.empty[Long]
  }

  def asDouble(): Optional[java.lang.Double] = this match {
    case d: IDouble => Optional.of(d.value)
    case _          => Optional.empty[java.lang.Double]
  }

  def asObject(): Optional[java.util.Map[String, IdmlValue]] = this match {
    case o: IObject => Optional.of(o.fields.asJava)
    case _          => Optional.empty[java.util.Map[String, IdmlValue]]
  }

  def asList(): Optional[java.util.List[IdmlValue]] = this match {
    case a: IArray => Optional.of(a.items.asJava)
    case _         => Optional.empty[java.util.List[IdmlValue]]
  }

}
