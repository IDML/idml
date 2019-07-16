package io.idml.jackson

import java.io.IOException

import com.fasterxml.jackson.core.{JsonParseException, JsonParser}
import com.fasterxml.jackson.databind.{JsonMappingException, ObjectMapper}
import io.idml._
import io.idml.jackson.serder.PtolemyJacksonModule

/** The Ptolemy json module. Usually you can just use PtolemyJson */
class PtolemyJackson(mapper: ObjectMapper) extends PtolemyJson {

  def this() {
    this(PtolemyJackson.newDefaultObjectMapper)
  }

  /** Take a json string and transform it into a DataNode hierarchy */
  @throws[PtolemyJsonReadingException]("if the JSON doesn't parse")
  override def parse(in: String): PtolemyValue = try {
    mapper.readValue(in, classOf[PtolemyValue])
  } catch {
    case e @ (_: JsonParseException | _: JsonMappingException | _: IOException) =>
      throw new PtolemyJsonReadingException(e)
  }

  /** Take a json string and transform it into a DataNode hierarchy, if it's an object */
  @throws[PtolemyJsonReadingException]("if the JSON doesn't parse")
  @throws[PtolemyJsonObjectException]("if it's not an object")
  override def parseObject(in: String): PtolemyObject =
    try {
      mapper.readValue(in, classOf[PtolemyObject]) match {
        case o: PtolemyObject => o
        case _ => throw new PtolemyJsonObjectException()
      }
    } catch {
      case e @ (_: JsonParseException | _: JsonMappingException | _: IOException) =>
        throw new PtolemyJsonReadingException(e)
    }

  /** Render a DataNode hierarchy as a compacted json dom */
  override def compact(d: PtolemyValue): String = mapper.writeValueAsString(d)

  /** Render a DataNode hierarchy as a pretty-printed json dom */
  override def pretty(d: PtolemyValue): String = {
    val writer = mapper.writerWithDefaultPrettyPrinter()
    writer.writeValueAsString(d)
  }
}

object PtolemyJackson {

  lazy val default: PtolemyJson = new PtolemyJackson(newDefaultObjectMapper)

  /** Create a new object mapper */
  def newDefaultObjectMapper: ObjectMapper =
    new ObjectMapper()
      .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
      .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
      .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
      .configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)
      .registerModule(new PtolemyJacksonModule)
}
