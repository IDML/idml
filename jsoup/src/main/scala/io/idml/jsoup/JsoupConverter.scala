package io.idml.jsoup

import io.idml.datanodes.{IDomElement, IDomText}
import io.idml.IdmlValue
import org.jsoup.nodes.{Element, TextNode}

import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import cats._, cats.data._, cats.implicits._

object JsoupConverter {
  def apply(el: Element): IdmlValue = go(el).value

  // Recursive implementation of the tree transform that uses Eval to be stack-safe
  private def go(el: Element): Eval[IDomElement] = {
    val name     = el.tagName()
    val attrs    = el.attributes().asList().asScala.map { a => a.getKey -> a.getValue }.toMap
    val children = el.childNodes().asScala.toList.traverse {
      case t: TextNode => Eval.now(Some(IDomText(t.getWholeText)))
      case e: Element  => go(e).map(Some(_))
      case _           => Eval.now(None)
    }
    children.map { c =>
      IDomElement(name, attrs, c.flatten)
    }
  }
}
