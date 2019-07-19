package io.idml.functions

import io.idml.ast.{Argument, IdmlFunctionMetadata, Pipeline}
import io.idml.IdmlValue

/** Alternative matcher to create IdmlValueFunction objects for functions which take N arguments */
class IdmlValueNaryFunctionResolver extends FunctionResolver {

  /** An extractor that acts as a piece of syntactic sugar for evaluating IdmlValue functions */
  def resolve(name: String, args: List[Argument]): Option[IdmlValueFunction] =
    args match {
      case as if as.forall(_.isInstanceOf[Pipeline]) =>
        try {
          val method =
            classOf[IdmlValue].getMethod(name, classOf[Seq[IdmlValue]])
          try Some(IdmlValueFunction(method, as.asInstanceOf[List[Pipeline]], isNAry = true))
          catch {
            case _: IllegalArgumentException => None
          }
        } catch {
          case _: NoSuchMethodException => None
        }
      case _ => None

    }
  override def providedFunctions(): List[IdmlFunctionMetadata] = List.empty // todo
}
