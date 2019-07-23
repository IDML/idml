package io.idml.jackson.serder

import io.idml.{IdmlArray, IdmlBool, IdmlDouble, IdmlInt, IdmlNothing, IdmlNull, IdmlObject, IdmlString, IdmlValue}
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

/** The Jackson serializer for PValues */
class PValueSerializer extends JsonSerializer[IdmlValue] {
  def serialize(value: IdmlValue, json: JsonGenerator, provider: SerializerProvider) {
    value match {
      case n: IdmlInt    => json.writeNumber(n.value)
      case n: IdmlDouble => json.writeNumber(n.value)
      case n: IdmlString => json.writeString(n.value)
      case n: IdmlBool   => json.writeBoolean(n.value)

      case n: IdmlArray =>
        json.writeStartArray()
        n.items filterNot (_.isInstanceOf[IdmlNothing]) foreach json.writeObject
        json.writeEndArray()

      case n: IdmlObject =>
        json.writeStartObject()
        n.fields.filterNot(_._2.isInstanceOf[IdmlNothing]).toList.sortBy(_._1).foreach {
          case (k, v) =>
            json.writeObjectField(k, v)
        }
        json.writeEndObject()

      case _: IdmlNothing => ()
      case IdmlNull       => json.writeNull()
    }
  }

  override def isEmpty(value: IdmlValue): Boolean =
    value.isInstanceOf[IdmlNothing]
}
