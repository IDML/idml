package io.idml.jsoup

import io.idml.{MissingField, PtolemyArray, PtolemyValue}
import org.jsoup.nodes.Element
import org.jsoup.parser.Tag
import org.jsoup.select.Elements

import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import scala.collection.mutable

class JsoupSelection(elements: mutable.Buffer[Element]) extends PtolemyArray {

  def this(elements: Elements) {
    this(mutable.Buffer(elements.asScala.toSeq: _*))
  }

  def items: mutable.Buffer[PtolemyValue] = elements.map(new JsoupElement(_))

  override def get(name: String): PtolemyValue = {
    name match {
      case "attribs" => ???
      case _ =>
        val selected = new Element(Tag.valueOf(name), "")
        var found    = false
        for (element <- elements;
             child   <- element.childNodes().asScala) {
          child match {
            case element: Element if element.tagName() == name =>
              selected.appendChild(child.clone())
              found = true
            case _ =>
          }
        }

        if (found) {
          new JsoupElement(selected)
        } else {
          MissingField
        }

    }
  }
}
