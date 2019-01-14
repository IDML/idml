package io.idml.functions

import io.idml.ast.{Argument, Pipeline, PtolemyFunction, PtolemyFunctionMetadata}

/** A class that may be able to resolve a function */
abstract class FunctionResolver {
  def providedFunctions(): List[PtolemyFunctionMetadata]
  def resolve(name: String, args: List[Argument]): Option[PtolemyFunction]
}
