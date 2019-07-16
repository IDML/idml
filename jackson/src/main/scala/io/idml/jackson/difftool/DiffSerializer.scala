package io.idml.jackson.difftool

import io.idml.{PtolemyArray, PtolemyNothing, PtolemyValue}
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import io.idml.jackson.serder.PValueSerializer

class DiffSerializer extends PValueSerializer {
  override def serialize(value: PtolemyValue, json: JsonGenerator, provider: SerializerProvider) {
    value match {
      case arr: PtolemyArray if Diff.isDiff(arr) =>
        val l = arr.get(1)
        val r = arr.get(2)
        if (!l.isInstanceOf[PtolemyNothing]) {
          json.writeRaw("<removed>")
          serialize(l, json, provider)
          json.writeRaw("</removed>")
        }
        if (!r.isInstanceOf[PtolemyNothing]) {
          json.writeRaw("<added>")
          serialize(r, json, provider)
          json.writeRaw("</added>")
        }
      case _ =>
        super.serialize(value, json, provider)
    }
  }

  override def isEmpty(value: PtolemyValue): Boolean =
    value.isInstanceOf[PtolemyNothing]
}
