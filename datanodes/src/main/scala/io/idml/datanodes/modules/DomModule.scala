package io.idml.datanodes.modules

import io.idml.{CastUnsupported, IdmlValue, MissingField}
import io.idml.datanodes.{IDomElement, IDomNode, IString}

trait DomModule {
  this: IdmlValue =>

  def tagName: IdmlValue = this match {
    case e: IDomElement => Option(e.name).map(IString).getOrElse(MissingField)
    case _ => CastUnsupported
  }

  // This is overridden in the DOM element type
  def attributes(): IdmlValue = MissingField

  def text: IdmlValue = this match {
    case e: IDomNode =>
      e.getText
    case _ => CastUnsupported
  }
}
