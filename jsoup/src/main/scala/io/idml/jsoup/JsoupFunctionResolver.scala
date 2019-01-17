package io.idml.jsoup

import io.idml.ast.{Argument, PtolemyFunction, PtolemyFunctionMetadata}
import io.idml.functions.FunctionResolver

class JsoupFunctionResolver extends FunctionResolver {
  override def resolve(name: String, args: List[Argument]): Option[PtolemyFunction] = {
    (name, args) match {
      case ("stripTags", Nil) => Some(StripTagsFunction)
      case _                  => None
    }
  }
  override def providedFunctions(): List[PtolemyFunctionMetadata] = List(
    PtolemyFunctionMetadata("stripTags", List.empty, "remove XML tags from this string")
  )
}
