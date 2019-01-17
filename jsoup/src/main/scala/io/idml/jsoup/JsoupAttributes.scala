package io.idml.jsoup

import io.idml.datanodes.PString
import io.idml.{PtolemyObject, PtolemyValue}
import org.jsoup.nodes.{Attribute, Attributes}

import scala.collection.mutable

import scala.collection.JavaConverters.iterableAsScalaIterableConverter

/**
  * Encapsulates the attributes on a collection of XML nodes
  *
  * @param attributes The XML elements to wrap
  */
class JsoupAttributes(val attributes: Attributes) extends PtolemyObject {
  // scalastyle:off null
  require(attributes != null)
  // scalastyle:on null

  /** The underlying field container for this object */
  override val fields: mutable.Map[String, PtolemyValue] =
    mutable.Map[String, PtolemyValue](attributes.asScala.map {
      case attrib: Attribute => attrib.getKey -> PString(attrib.getValue)
    }.toSeq: _*)
}
