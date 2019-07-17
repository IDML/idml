package io.idml.functions

import java.lang.reflect.Modifier

import io.idml.IdmlValue
import io.idml.ast.{Argument, IdmlFunctionMetadata, Pipeline}

import scala.collection.JavaConverters._

/** Companion object for IdmlValueFunction */
class IdmlValueFunctionResolver extends FunctionResolver {

  /** An extractor that acts as a piece of syntactic sugar for evaluating IdmlValue functions */
  def resolve(name: String, args: List[Argument]): Option[IdmlValueFunction] =
    args match {
      case as if as.forall(_.isInstanceOf[Pipeline]) =>
        try {
          val classes = as.map(x => classOf[IdmlValue])
          val method  = classOf[IdmlValue].getMethod(name, classes: _*)
          try Some(IdmlValueFunction(method, as.asInstanceOf[List[Pipeline]]))
          catch {
            case _: IllegalArgumentException => None
          }
        } catch {
          case _: NoSuchMethodException => None
        }
      case _ => None
    }

  lazy val functions = classOf[IdmlValue]
    .getMethods()
    .filter(_.getReturnType == classOf[IdmlValue])
    .map { m =>
      IdmlFunctionMetadata(m.getName, m.getParameters.map(_.getName -> "").toList, "")
    }
    .toSet
    .toList
  override def providedFunctions(): List[IdmlFunctionMetadata] = functions
}
