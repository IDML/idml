package io.idml.functions.json

import java.nio.charset.StandardCharsets.UTF_8
import java.util.UUID

import io.idml.datanodes.IString
import io.idml.functions.IdmlFunction0
import io.idml.utils.IdmlUUID
import io.idml.{CastUnsupported, IdmlJson, IdmlObject, IdmlString, IdmlValue}

class UUIDModuleJson(json: IdmlJson) extends ObjectModuleJson(json) {

  val uuid3Function: IdmlFunction0 = new IdmlFunction0 {
    override protected def apply(cursor: IdmlValue): IdmlValue =
      cursor match {
        case o: IdmlObject =>
          IString(UUID.nameUUIDFromBytes(serialize(o).toStringOption.get.getBytes(UTF_8)).toString)
        case n: IdmlString => IString(UUID.nameUUIDFromBytes(n.value.getBytes(UTF_8)).toString)
        case _             => CastUnsupported
      }
    override def name: String                                  = "uuid3"
  }

  val uuid5Function: IdmlFunction0 = new IdmlFunction0 {
    override protected def apply(cursor: IdmlValue): IdmlValue =
      cursor match {
        case o: IdmlObject =>
          IString(
            IdmlUUID.nameUUIDFromBytes5(serialize(o).toStringOption.get.getBytes(UTF_8)).toString)
        case n: IdmlString => IString(IdmlUUID.nameUUIDFromBytes5(n.value.getBytes(UTF_8)).toString)
      }
    override def name: String                                  = "uuid5"
  }
}
