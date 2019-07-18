package io.idml.utils

import io.idml.datanodes.IObject
import io.idml._
import io.idml.ast.{Argument, IdmlFunction, IdmlFunctionMetadata, Pipeline}
import io.idml.functions.FunctionResolver

import scala.collection.mutable

object AutoComplete {

  def complete(ptolemy: Idml)(doc: IdmlObject, document: String, cursor: Int) = {
    val newdoc = document.slice(0, cursor) + "analyse()" + document.slice(cursor, document.length)
    val ctx    = new IdmlContext()
    ctx.state.put(AnalyseFunction.AnalysisState, mutable.Buffer[IdmlValue]())
    ctx.input = doc
    ctx.scope = doc
    ptolemy.compile(newdoc).run(ctx)
    ctx.state
      .get(AnalyseFunction.AnalysisState)
      .asInstanceOf[Option[mutable.Buffer[IdmlValue]]]
      .getOrElse(mutable.Buffer.empty[IdmlValue])
      .toList
      .flatMap {
        case o: IObject =>
          o.fields.keys.toList
        case _ =>
          List.empty
      }
      .toSet
  }
}

class AnalysisModule extends FunctionResolver {
  override def providedFunctions(): List[IdmlFunctionMetadata] = List.empty // we don't reveal this module to discoverability
  override def resolve(name: String, args: List[Argument]): Option[IdmlFunction] = (name, args) match {
    case ("analyse", Nil) => Some(AnalyseFunction)
    case _                => None
  }
}

object AnalyseFunction extends IdmlFunction {
  case object AnalysisState

  override def name: String = "analyse"
  override def invoke(ctx: IdmlContext): Unit = {
    List(ctx.cursor, ctx.scope, ctx.input).find(_ != IdmlNull).foreach { r =>
      ctx.state.get(AnalysisState).get.asInstanceOf[mutable.Buffer[IdmlValue]].append(r.deepCopy)
    }
  }
  override def args: List[Pipeline] = List.empty
}
