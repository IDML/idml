package io.idml.jackson

import java.io.IOException

import com.fasterxml.jackson.core.{JsonParseException, JsonParser}
import com.fasterxml.jackson.databind.{JsonMappingException, ObjectMapper}
import io.idml._
import io.idml.jackson.serder.IdmlJacksonModule

/** The Idml json module. Usually you can just use IdmlJson */
class IdmlJackson(mapper: ObjectMapper) extends IdmlJson {

  def this() {
    this(IdmlJackson.newDefaultObjectMapper)
  }

  /** Take a json string and transform it into a DataNode hierarchy */
  @throws[IdmlJsonReadingException]("if the JSON doesn't parse")
  override def parse(in: String): IdmlValue =
    try {
      mapper.readValue(in, classOf[IdmlValue])
    } catch {
      case e @ (_: JsonParseException | _: JsonMappingException | _: IOException) =>
        throw new IdmlJsonReadingException(e)
    }

  /** Take a json string and transform it into a DataNode hierarchy, if it's an object */
  @throws[IdmlJsonReadingException]("if the JSON doesn't parse")
  @throws[IdmlJsonObjectException]("if it's not an object")
  override def parseObject(in: String): IdmlObject =
    try {
      mapper.readValue(in, classOf[IdmlValue]) match {
        case o: IdmlObject => o
        case _             => throw new IdmlJsonObjectException()
      }
    } catch {
      case e @ (_: JsonParseException | _: JsonMappingException | _: IOException) =>
        throw new IdmlJsonReadingException(e)
    }

  /** Render a DataNode hierarchy as a compacted json dom */
  override def compact(d: IdmlValue): String = mapper.writeValueAsString(d)

  /** Render a DataNode hierarchy as a pretty-printed json dom */
  override def pretty(d: IdmlValue): String = {
    val writer = mapper.writerWithDefaultPrettyPrinter()
    writer.writeValueAsString(d)
  }
}

object IdmlJackson {

  lazy val default: IdmlJson = new IdmlJackson(newDefaultObjectMapper)

  /** Create a new object mapper */
  def newDefaultObjectMapper: ObjectMapper =
    new ObjectMapper()
      .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
      .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
      .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
      .configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)
      .registerModule(new IdmlJacksonModule)
}
