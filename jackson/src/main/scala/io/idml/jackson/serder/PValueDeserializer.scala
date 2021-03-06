package io.idml.jackson.serder

import io.idml.datanodes.{IArray, IDouble, IFalse, IInt, IObject, IString, ITrue}
import io.idml.{IdmlNull, IdmlValue}
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
      case JsonToken.VALUE_NULL         => IdmlNull
      case JsonToken.VALUE_NUMBER_INT   => new IInt(jp.getLongValue)
      case JsonToken.VALUE_NUMBER_FLOAT => new IDouble(jp.getValueAsDouble)
      case JsonToken.VALUE_STRING       => new IString(jp.getText)
      case JsonToken.VALUE_TRUE         => ITrue
      case JsonToken.VALUE_FALSE        => IFalse

      case JsonToken.START_ARRAY =>
        startArray(jp, ctxt)

      case JsonToken.START_OBJECT =>
        startObject(jp, ctxt)

      case JsonToken.FIELD_NAME | JsonToken.END_OBJECT =>
        fieldNameOrEndObject(jp, ctxt)

      case _ => throw ctxt.mappingException(classOf[IdmlValue])
    }

    if (!klass.isAssignableFrom(value.getClass))
      throw ctxt.mappingException(klass)

    value
  }

  def startObject(jp: JsonParser, ctxt: DeserializationContext): Object = {
    jp.nextToken()
    deserialize(jp, ctxt)
  }

  def startArray(jp: JsonParser, ctxt: DeserializationContext): IArray = {
    val values = mutable.Buffer[IdmlValue]()
    jp.nextToken()
    while (jp.getCurrentToken != JsonToken.END_ARRAY) {
      values += deserialize(jp, ctxt).asInstanceOf[IdmlValue]
      jp.nextToken()
    }
    new IArray(values)
  }

  def fieldNameOrEndObject(jp: JsonParser, ctxt: DeserializationContext): IObject = {
    val fields = mutable.Map[String, IdmlValue]()
    while (jp.getCurrentToken != JsonToken.END_OBJECT) {
      val name = jp.getCurrentName
      jp.nextToken()
      fields.put(name, deserialize(jp, ctxt).asInstanceOf[IdmlValue])
      jp.nextToken()
    }
    new IObject(fields)
  }

  override def isCachable: Boolean = true

  override def getNullValue: Object = IdmlNull

  // TODO Need to fully investigate this
  override def getEmptyValue: Object = IdmlNull
}
