package io.idml.utils
import io.idml.{jackson, _}
import io.idml.ast._

import scala.collection.mutable
import scala.collection.JavaConverters._

object Tracer {
  class Annotator extends PtolemyListener {
    val results = mutable.ListBuffer[(Position, PtolemyValue)]()

    override def exitAssignment(ctx: PtolemyContext, assignment: Assignment): Unit = {
      assignment.positions.map(p => (p.end, ctx.cursor)).foreach(results.append(_))
    }
    override def enterAssignment(ctx: PtolemyContext, assignment: Assignment): Unit = ()
    override def enterChain(ctx: PtolemyContext): Unit                              = ()
    override def exitChain(ctx: PtolemyContext): Unit                               = ()
    override def enterPath(context: PtolemyContext, path: Field): Unit              = ()
    override def exitPath(context: PtolemyContext, path: Field): Unit               = ()
    override def enterPipl(context: PtolemyContext, pipl: Pipeline): Unit           = ()
    override def exitPipl(context: PtolemyContext, pipl: Pipeline): Unit            = ()
    override def enterFunc(ctx: PtolemyContext, func: PtolemyFunction): Unit        = ()
    override def exitFunc(ctx: PtolemyContext, func: PtolemyFunction): Unit         = ()
    override def enterMaths(context: PtolemyContext, maths: Maths): Unit            = ()
    override def exitMaths(context: PtolemyContext, maths: Maths): Unit             = ()

    def clear() = results.clear()

    def render(s: String): String = {
      val output = s.lines.toArray
      results.foreach {
        case (Position(line, char), r) =>
          output(line - 1) = output(line - 1) + " # " + jackson.PtolemyJson.compact(r)
      }
      output.mkString("\n")
    }
  }

  def annotate(s: String)(j: String): String = {
    val a   = new Annotator()
    val p   = new Ptolemy(new PtolemyConf(), List[PtolemyListener](a).asJava)
    val ctx = new PtolemyContext(jackson.PtolemyJson.parse(j), jackson.PtolemyJson.newObject(), List[PtolemyListener](a))
    p.fromString(s).run(ctx).output
    a.render(s)
  }

}
