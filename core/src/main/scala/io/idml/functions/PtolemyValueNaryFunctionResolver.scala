package io.idml.functions

import io.idml.ast.{Argument, Pipeline, PtolemyFunctionMetadata}
import io.idml.PtolemyValue

/** Alternative matcher to create PtolemyValueFunction objects for functions which take N arguments */
class PtolemyValueNaryFunctionResolver extends FunctionResolver {

  /** An extractor that acts as a piece of syntactic sugar for evaluating PtolemyValue functions */
  def resolve(name: String, args: List[Argument]): Option[PtolemyValueFunction] =
    args match {
      case as if as.forall(_.isInstanceOf[Pipeline]) =>
        try {
          val method =
            classOf[PtolemyValue].getMethod(name, classOf[Seq[PtolemyValue]])
          try Some(PtolemyValueFunction(method, as.asInstanceOf[List[Pipeline]], isNAry = true))
          catch {
            case _: IllegalArgumentException => None
          }
        } catch {
          case _: NoSuchMethodException => None
        }
      case _ => None

    }
  override def providedFunctions(): List[PtolemyFunctionMetadata] = List.empty // todo
}
