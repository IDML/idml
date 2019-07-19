package io.idml.functions

import io.idml.ast.{Argument, IdmlFunction, IdmlFunctionMetadata, Pipeline}

/** A class that may be able to resolve a function */
abstract class FunctionResolver {
  def providedFunctions(): List[IdmlFunctionMetadata]
  def resolve(name: String, args: List[Argument]): Option[IdmlFunction]
}
