package io.idml.jsoup

import io.idml.datanodes.PString
import io.idml.{IdmlArray, IdmlObject, IdmlValue, MissingField}
import org.jsoup.nodes.{Element, TextNode}

import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import scala.collection.mutable

/**
  * Encapsulates an XML element as an json-ml array
  *
  * @param element The underlying XML
  */
class JsoupElement(val element: Element) extends IdmlArray {
  // scalastyle:off null
  require(element != null)
  // scalastyle:on null

  /** Tag name */
  val tag = PString(element.tagName())

  /** Attributes */
  lazy val attributes: IdmlObject = new JsoupAttributes(element.attributes())

  /** Children */
  lazy val children = {
    element.childNodes().asScala.map {
      case text: TextNode   => PString(text.text())
      case element: Element => new JsoupElement(element)
      case other: Any =>
        throw new IllegalArgumentException(s"Unsupported: $other")
    }
  }.toSeq

  /** The underlying items array */
  override lazy val items = {
    val buf = mutable.Buffer[IdmlValue](tag)
    if (attributes.fields.nonEmpty) {
      buf.append(attributes)
    }
    buf.appendAll(children)
    buf
  }

  /**
    * Find one or more nested tags. get('attribs') is a special case that returns the list of tag attributes.
    *
    * @param name The name of the tag
    * @return A new cursor
    */
  override def get(name: String): IdmlValue = {
    name match {
      case "attribs" => attributes
      case _ =>
        val selected = mutable.Buffer[Element]()
        for (child <- element.childNodes().asScala) {
          child match {
            case element: Element if element.tagName() == name =>
              selected += element
            case _ =>
          }
        }

        if (selected.nonEmpty) {
          new JsoupSelection(selected)
        } else {
          MissingField
        }

    }
  }
}
