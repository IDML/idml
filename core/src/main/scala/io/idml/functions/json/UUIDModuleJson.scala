package io.idml.functions.json

import java.nio.charset.Charset
import java.util.UUID

import io.idml.datanodes.PString
import io.idml.functions.IdmlFunction0
import io.idml.utils.IdmlUUID
import io.idml.{CastUnsupported, IdmlJson, IdmlObject, IdmlString, IdmlValue}

class UUIDModuleJson(json: IdmlJson) extends ObjectModuleJson(json) {

  val uuid3Function: IdmlFunction0 = new IdmlFunction0 {
    override protected def apply(cursor: IdmlValue): IdmlValue = cursor match {
      case o: IdmlObject => PString(UUID.nameUUIDFromBytes(serialize(o).toStringOption.get.getBytes(Charset.defaultCharset())).toString)
      case n: IdmlString => PString(UUID.nameUUIDFromBytes(n.value.getBytes(Charset.defaultCharset())).toString)
      case _             => CastUnsupported
    }
    override def name: String = "uuid3"
  }

  val uuid5Function: IdmlFunction0 = new IdmlFunction0 {
    override protected def apply(cursor: IdmlValue): IdmlValue = cursor match {
      case o: IdmlObject =>
        PString(IdmlUUID.nameUUIDFromBytes5(serialize(o).toStringOption.get.getBytes(Charset.defaultCharset())).toString)
      case n: IdmlString => PString(IdmlUUID.nameUUIDFromBytes5(n.value.getBytes(Charset.defaultCharset())).toString)
    }
    override def name: String = "uuid5"
  }
}
