package io.idml.datanodes.serder

import io.idml.datanodes.{PArray, PDouble, PFalse, PInt, PObject, PString, PTrue}
import io.idml.{PtolemyNull, PtolemyValue}
import com.fasterxml.jackson.core.{JsonParser, JsonToken}
import com.fasterxml.jackson.databind.`type`.TypeFactory
import com.fasterxml.jackson.databind.{DeserializationContext, JsonDeserializer}

import scala.collection.mutable

/** The Jackson de-serializer for PValues */
class PValueDeserializer(factory: TypeFactory, klass: Class[_]) extends JsonDeserializer[Object] {
  def deserialize(jp: JsonParser, ctxt: DeserializationContext): Object = {

    // scalastyle:off null
    if (jp.getCurrentToken == null) jp.nextToken()
    // scalastyle:on null

    val value = jp.getCurrentToken match {
      case JsonToken.VALUE_NULL         => PtolemyNull
      case JsonToken.VALUE_NUMBER_INT   => new PInt(jp.getLongValue)
      case JsonToken.VALUE_NUMBER_FLOAT => new PDouble(jp.getValueAsDouble)
      case JsonToken.VALUE_STRING       => new PString(jp.getText)
      case JsonToken.VALUE_TRUE         => PTrue
      case JsonToken.VALUE_FALSE        => PFalse

      case JsonToken.START_ARRAY =>
        startArray(jp, ctxt)

      case JsonToken.START_OBJECT =>
        startObject(jp, ctxt)

      case JsonToken.FIELD_NAME | JsonToken.END_OBJECT =>
        fieldNameOrEndObject(jp, ctxt)

      case _ => throw ctxt.mappingException(classOf[PtolemyValue])
    }

    if (!klass.isAssignableFrom(value.getClass))
      throw ctxt.mappingException(klass)

    value
  }

  def startObject(jp: JsonParser, ctxt: DeserializationContext): Object = {
    jp.nextToken()
    deserialize(jp, ctxt)
  }

  def startArray(jp: JsonParser, ctxt: DeserializationContext): PArray = {
    val values = mutable.Buffer[PtolemyValue]()
    jp.nextToken()
    while (jp.getCurrentToken != JsonToken.END_ARRAY) {
      values += deserialize(jp, ctxt).asInstanceOf[PtolemyValue]
      jp.nextToken()
    }
    new PArray(values)
  }

  def fieldNameOrEndObject(jp: JsonParser, ctxt: DeserializationContext): PObject = {
    val fields = mutable.SortedMap[String, PtolemyValue]()
    while (jp.getCurrentToken != JsonToken.END_OBJECT) {
      val name = jp.getCurrentName
      jp.nextToken()
      fields.put(name, deserialize(jp, ctxt).asInstanceOf[PtolemyValue])
      jp.nextToken()
    }
    new PObject(fields)
  }

  override def isCachable: Boolean = true

  override def getNullValue: Object = PtolemyNull

  // TODO Need to fully investigate this
  override def getEmptyValue: Object = PtolemyNull
}
