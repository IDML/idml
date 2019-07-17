package io.idml.jackson.serder

import io.idml.{
  PtolemyArray,
  PtolemyBool,
  PtolemyDouble,
  PtolemyInt,
  PtolemyNothing,
  PtolemyNull,
  PtolemyObject,
  PtolemyString,
  PtolemyValue
}
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

/** The Jackson serializer for PValues */
class PValueSerializer extends JsonSerializer[PtolemyValue] {
  def serialize(value: PtolemyValue, json: JsonGenerator, provider: SerializerProvider) {
    value match {
      case n: PtolemyInt    => json.writeNumber(n.value)
      case n: PtolemyDouble => json.writeNumber(n.value)
      case n: PtolemyString => json.writeString(n.value)
      case n: PtolemyBool   => json.writeBoolean(n.value)

      case n: PtolemyArray =>
        json.writeStartArray()
        n.items filterNot (_.isInstanceOf[PtolemyNothing]) foreach json.writeObject
        json.writeEndArray()

      case n: PtolemyObject =>
        json.writeStartObject()
        n.fields filterNot (_._2.isInstanceOf[PtolemyNothing]) foreach {
          case (k, v) =>
            json.writeObjectField(k, v)
        }
        json.writeEndObject()

      case _: PtolemyNothing => ()
      case PtolemyNull       => json.writeNull()
    }
  }

  override def isEmpty(value: PtolemyValue): Boolean =
    value.isInstanceOf[PtolemyNothing]
}
