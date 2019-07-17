package io.idml.functions.json

import java.nio.charset.Charset
import java.util.UUID

import io.idml.datanodes.PString
import io.idml.functions.PtolemyFunction0
import io.idml.utils.PtolemyUUID
import io.idml.{CastUnsupported, PtolemyJson, PtolemyObject, PtolemyString, PtolemyValue}

class UUIDModuleJson(json: PtolemyJson) extends ObjectModuleJson(json) {

  val uuid3Function: PtolemyFunction0 = new PtolemyFunction0 {
    override protected def apply(cursor: PtolemyValue): PtolemyValue = cursor match {
      case o: PtolemyObject => PString(UUID.nameUUIDFromBytes(serialize(o).toStringOption.get.getBytes(Charset.defaultCharset())).toString)
      case n: PtolemyString => PString(UUID.nameUUIDFromBytes(n.value.getBytes(Charset.defaultCharset())).toString)
      case _                => CastUnsupported
    }
    override def name: String = "uuid3"
  }

  val uuid5Function: PtolemyFunction0 = new PtolemyFunction0 {
    override protected def apply(cursor: PtolemyValue): PtolemyValue = cursor match {
      case o: PtolemyObject =>
        PString(PtolemyUUID.nameUUIDFromBytes5(serialize(o).toStringOption.get.getBytes(Charset.defaultCharset())).toString)
      case n: PtolemyString => PString(PtolemyUUID.nameUUIDFromBytes5(n.value.getBytes(Charset.defaultCharset())).toString)
    }
    override def name: String = "uuid5"
  }
}
