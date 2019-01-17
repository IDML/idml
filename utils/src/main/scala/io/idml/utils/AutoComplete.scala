package io.idml.utils

import io.idml.datanodes.PObject
import io.idml._
import io.idml.ast.{Argument, Pipeline, PtolemyFunction, PtolemyFunctionMetadata}
import io.idml.functions.FunctionResolver

import scala.collection.mutable

object AutoComplete {

  def complete(ptolemy: Ptolemy)(doc: PtolemyObject, document: String, cursor: Int) = {
    val newdoc = document.slice(0, cursor) + "analyse()" + document.slice(cursor, document.length)
    val ctx    = new PtolemyContext()
    ctx.state.put(AnalyseFunction.AnalysisState, mutable.Buffer[PtolemyValue]())
    ctx.input = doc
    ctx.scope = doc
    ptolemy.fromString(newdoc).run(ctx)
    ctx.state
      .get(AnalyseFunction.AnalysisState)
      .asInstanceOf[Option[mutable.Buffer[PtolemyValue]]]
      .getOrElse(mutable.Buffer.empty[PtolemyValue])
      .toList
      .flatMap {
        case o: PObject =>
          o.fields.keys.toList
        case _ =>
          List.empty
      }
      .toSet
  }
}

class AnalysisModule extends FunctionResolver {
  override def providedFunctions(): List[PtolemyFunctionMetadata] = List.empty // we don't reveal this module to discoverability
  override def resolve(name: String, args: List[Argument]): Option[PtolemyFunction] = (name, args) match {
    case ("analyse", Nil) => Some(AnalyseFunction)
    case _                => None
  }
}

object AnalyseFunction extends PtolemyFunction {
  case object AnalysisState

  override def name: String = "analyse"
  override def invoke(ctx: PtolemyContext): Unit = {
    List(ctx.cursor, ctx.scope, ctx.input).find(_ != PtolemyNull).foreach { r =>
      ctx.state.get(AnalysisState).get.asInstanceOf[mutable.Buffer[PtolemyValue]].append(r.deepCopy)
    }
  }
  override def args: List[Pipeline] = List.empty
}
