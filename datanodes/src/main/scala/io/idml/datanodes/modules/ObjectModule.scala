package io.idml.datanodes.modules

import io.idml.datanodes.{PArray, PString}
import io.idml._

import scala.util.Try

/** Adds object-like behaviour */
trait ObjectModule {
  this: IdmlValue =>

  /** Remove a field by name */
  def remove(path: String) {}

  def values(): IdmlValue = this match {
    case o: IdmlObject => PArray(o.fields.values.toBuffer)
    case _             => InvalidCaller
  }

  def keys(): IdmlValue = this match {
    case o: IdmlObject => PArray(o.fields.keys.map(PString.apply).toBuffer[IdmlValue])
    case _             => InvalidCaller
  }

}
