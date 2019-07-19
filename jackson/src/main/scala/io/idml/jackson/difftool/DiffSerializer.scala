package io.idml.jackson.difftool

import io.idml.{IdmlArray, IdmlNothing, IdmlValue}
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import io.idml.jackson.serder.PValueSerializer

class DiffSerializer extends PValueSerializer {
  override def serialize(value: IdmlValue, json: JsonGenerator, provider: SerializerProvider) {
    value match {
      case arr: IdmlArray if Diff.isDiff(arr) =>
        val l = arr.get(1)
        val r = arr.get(2)
        if (!l.isInstanceOf[IdmlNothing]) {
          json.writeRaw("<removed>")
          serialize(l, json, provider)
          json.writeRaw("</removed>")
        }
        if (!r.isInstanceOf[IdmlNothing]) {
          json.writeRaw("<added>")
          serialize(r, json, provider)
          json.writeRaw("</added>")
        }
      case _ =>
        super.serialize(value, json, provider)
    }
  }

  override def isEmpty(value: IdmlValue): Boolean =
    value.isInstanceOf[IdmlNothing]
}
