package io.idml.jsoup

import io.idml.ast.{Argument, IdmlFunction, IdmlFunctionMetadata}
import io.idml.functions.FunctionResolver

class JsoupFunctionResolver extends FunctionResolver {
  override def resolve(name: String, args: List[Argument]): Option[IdmlFunction] = {
    (name, args) match {
      case ("stripTags", Nil) => Some(StripTagsFunction)
      case _                  => None
    }
  }
  override def providedFunctions(): List[IdmlFunctionMetadata] = List(
    IdmlFunctionMetadata("stripTags", List.empty, "remove XML tags from this string")
  )
}
