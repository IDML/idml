package io.idml.functions

import io.idml.PtolemyValue
import io.idml.ast.{Argument, Pipeline, PtolemyFunctionMetadata}

import scala.collection.JavaConverters._

/** Companion object for PtolemyValueFunction */
class PtolemyValueFunctionResolver extends FunctionResolver {

  /** An extractor that acts as a piece of syntactic sugar for evaluating PtolemyValue functions */
  def resolve(name: String, args: List[Argument]): Option[PtolemyValueFunction] =
    args match {
      case as if as.forall(_.isInstanceOf[Pipeline]) =>
        try {
          val classes = as.map(x => classOf[PtolemyValue])
          val method  = classOf[PtolemyValue].getMethod(name, classes: _*)
          try Some(PtolemyValueFunction(method, as.asInstanceOf[List[Pipeline]]))
          catch {
            case _: IllegalArgumentException => None
          }
        } catch {
          case _: NoSuchMethodException => None
        }
      case _ => None
    }

  lazy val functions = classOf[PtolemyValue]
    .getMethods()
    .filter(_.getReturnType == classOf[PtolemyValue])
    .map { m =>
      PtolemyFunctionMetadata(m.getName, m.getParameters.map(_.getName -> "").toList, "")
    }
    .toSet
    .toList
  override def providedFunctions(): List[PtolemyFunctionMetadata] = functions
}
