package io.idml.datanodes.modules

import java.util.Optional
import java.lang._

import io.idml.IdmlValue
import io.idml.datanodes.{PArray, PBool, PDouble, PInt, PObject, PString}

import scala.collection.JavaConverters._

trait JavaApiModule {
  this: IdmlValue =>

  def asBoolean(): Optional[Boolean] = this match {
    case b: PBool => Optional.of(b.value.asInstanceOf[java.lang.Boolean])
    case _        => Optional.empty[java.lang.Boolean]
  }

  def asString(): Optional[java.lang.String] = this match {
    case s: PString => Optional.of(s.value)
    case _          => Optional.empty[String]
  }

  def asLong(): Optional[java.lang.Long] = this match {
    case l: PInt => Optional.of(l.value)
    case _       => Optional.empty[Long]
  }

  def asDouble(): Optional[java.lang.Double] = this match {
    case d: PDouble => Optional.of(d.value)
    case _          => Optional.empty[java.lang.Double]
  }

  def asObject(): Optional[java.util.Map[String, IdmlValue]] = this match {
    case o: PObject => Optional.of(o.fields.asJava)
    case _          => Optional.empty[java.util.Map[String, IdmlValue]]
  }

  def asList(): Optional[java.util.List[IdmlValue]] = this match {
    case a: PArray => Optional.of(a.items.asJava)
    case _         => Optional.empty[java.util.List[IdmlValue]]
  }

}
