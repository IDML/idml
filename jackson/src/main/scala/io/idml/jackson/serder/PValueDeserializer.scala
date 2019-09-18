package io.idml.jackson.serder

import com.fasterxml.jackson.core.JsonParser.NumberType
import io.idml.datanodes._
import io.idml.{IdmlNull, IdmlValue}
import com.fasterxml.jackson.core.{JsonParseException, JsonParser, JsonToken}
import com.fasterxml.jackson.databind.`type`.TypeFactory
import com.fasterxml.jackson.databind.{DeserializationContext, JsonDeserializer}

import scala.collection.mutable
import scala.util.Try

/** The Jackson de-serializer for PValues */
class PValueDeserializer(factory: TypeFactory, klass: Class[_]) extends JsonDeserializer[Object] {

  private def digitCounter(s: String): Int = s.toLowerCase.split('e').headOption.map(_.count(_.isDigit)).getOrElse(0)

  def deserialize(jp: JsonParser, ctxt: DeserializationContext): Object = {

    // scalastyle:off null
    if (jp.getCurrentToken == null) jp.nextToken()
    // scalastyle:on null

    def mapNumber(t: NumberType, p: JsonParser): IdmlValue = t match {
      case NumberType.INT         => IInt(p.getLongValue)
      case NumberType.LONG        => IInt(p.getLongValue)
      case NumberType.BIG_INTEGER => IBigInt(p.getBigIntegerValue)
      case NumberType.FLOAT       => IDouble(p.getDoubleValue)
      case NumberType.DOUBLE if p.getDoubleValue.isInfinite =>
        Try {
          IBigDecimal(p.getDecimalValue)
        }.getOrElse(IDouble(p.getDoubleValue))
      case NumberType.DOUBLE if digitCounter(p.getText) < 16  => IDouble(p.getDoubleValue)
      case NumberType.DOUBLE if digitCounter(p.getText) >= 16 => IBigDecimal(p.getDecimalValue)
      case NumberType.BIG_DECIMAL                             => IBigDecimal(p.getDecimalValue)
    }

    val value = jp.getCurrentToken match {
      case JsonToken.VALUE_NULL => IdmlNull
      case JsonToken.VALUE_NUMBER_INT =>
        mapNumber(jp.getNumberType, jp)
      case JsonToken.VALUE_NUMBER_FLOAT =>
        mapNumber(jp.getNumberType, jp)
      case JsonToken.VALUE_STRING => new IString(jp.getText)
      case JsonToken.VALUE_TRUE   => ITrue
      case JsonToken.VALUE_FALSE  => IFalse

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
