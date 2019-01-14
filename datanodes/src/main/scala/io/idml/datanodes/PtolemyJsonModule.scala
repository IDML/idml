package io.idml.datanodes

import java.io.{File, InputStream, Reader}

import io.idml.datanodes.serder.PtolemyJacksonModule
import io.idml.PtolemyValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper

/** The Ptolemy json module. Usually you can just use PtolemyJson */
class PtolemyJsonModule(mapper: ObjectMapper) {

  def this() {
    this(PtolemyJsonModule.newDefaultObjectMapper)
  }

  /** Create a new Json object */
  def newObject(): PObject = PObject()

  /** Take a json string and transform it into a DataNode hierarchy */
  def parse(in: String): PtolemyValue =
    mapper.readValue(in, classOf[PtolemyValue])

  /** Take json from a Reader and transform it into a DataNode hierarchy */
  def parse(in: Reader): PtolemyValue =
    mapper.readValue(in, classOf[PtolemyValue])

  /** Take json from an InputStream and transform it into a DataNode hierarchy */
  def parse(in: InputStream): PtolemyValue =
    mapper.readValue(in, classOf[PtolemyValue])

  /** Take json from a File and transform it into a DataNode hierarchy */
  def parse(in: File): PtolemyValue =
    mapper.readValue(in, classOf[PtolemyValue])

  /** Render a DataNode hierarchy as a compacted json dom */
  def compact(d: PtolemyValue): String = mapper.writeValueAsString(d)

  /** Render a DataNode hierarchy as a pretty-printed json dom */
  def pretty(d: PtolemyValue): String = {
    val writer = mapper.writerWithDefaultPrettyPrinter()
    writer.writeValueAsString(d)
  }
}

object PtolemyJsonModule {

  /** Create a new object mapper */
  def newDefaultObjectMapper: ObjectMapper =
    new ObjectMapper()
      .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
      .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
      .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
      .configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)
      .registerModule(new PtolemyJacksonModule)
}
