package io.idml.test
import io.idml.{PtolemyContext, PtolemyValue}
import io.idml.ast.{Argument, Pipeline, PtolemyFunction, PtolemyFunctionMetadata}
import io.idml.datanodes.PDate
import io.idml.functions.{FunctionResolver, PtolemyFunction0}
import org.joda.time.{DateTime, DateTimeZone}

class DeterministicTime(val time: Long = 0) extends FunctionResolver {
  override def providedFunctions(): List[PtolemyFunctionMetadata] = List(
    PtolemyFunctionMetadata("now", List.empty, "output the current time in a deterministic way")
  )
  override def resolve(name: String, args: List[Argument]): Option[PtolemyFunction] = (name, args) match {
    case ("now", Nil)       => Some(DeterministicTime.now(time))
    case ("microtime", Nil) => Some(DeterministicTime.microtime(time))
    case _                  => None
  }
}

object DeterministicTime {
  def now(time: Long) = new PtolemyFunction0 {
    override protected def apply(cursor: PtolemyValue): PtolemyValue = PDate(new DateTime(time, DateTimeZone.UTC))
    override def name: String                                        = "now"
  }
  def microtime(time: Long) = new PtolemyFunction0 {
    override protected def apply(cursor: PtolemyValue): PtolemyValue = PtolemyValue(time * 1000)
    override def name: String                                        = "microtime"
  }
}
