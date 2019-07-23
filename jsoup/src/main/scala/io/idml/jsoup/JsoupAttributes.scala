package io.idml.jsoup

import io.idml.datanodes.IString
import io.idml.{IdmlObject, IdmlValue}
import org.jsoup.nodes.{Attribute, Attributes}

import scala.collection.mutable

import scala.collection.JavaConverters.iterableAsScalaIterableConverter

/**
  * Encapsulates the attributes on a collection of XML nodes
  *
  * @param attributes The XML elements to wrap
  */
class JsoupAttributes(val attributes: Attributes) extends IdmlObject {
  // scalastyle:off null
  require(attributes != null)
  // scalastyle:on null

  /** The underlying field container for this object */
  override val fields: mutable.Map[String, IdmlValue] =
    mutable.Map[String, IdmlValue](attributes.asScala.map {
      case attrib: Attribute => attrib.getKey -> IString(attrib.getValue)
    }.toSeq: _*)
}
