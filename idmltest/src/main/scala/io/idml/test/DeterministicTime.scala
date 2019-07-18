package io.idml.test
import io.idml.{IdmlContext, IdmlValue}
import io.idml.ast.{Argument, IdmlFunction, IdmlFunctionMetadata, Pipeline}
import io.idml.datanodes.IDate
import io.idml.functions.{FunctionResolver, IdmlFunction0}
import org.joda.time.{DateTime, DateTimeZone}

class DeterministicTime(val time: Long = 0) extends FunctionResolver {
  override def providedFunctions(): List[IdmlFunctionMetadata] = List(
    IdmlFunctionMetadata("now", List.empty, "output the current time in a deterministic way")
  )
  override def resolve(name: String, args: List[Argument]): Option[IdmlFunction] = (name, args) match {
    case ("now", Nil)       => Some(DeterministicTime.now(time))
    case ("microtime", Nil) => Some(DeterministicTime.microtime(time))
    case _                  => None
  }
}

object DeterministicTime {
  def now(time: Long) = new IdmlFunction0 {
    override protected def apply(cursor: IdmlValue): IdmlValue = IDate(new DateTime(time, DateTimeZone.UTC))
    override def name: String                                  = "now"
  }
  def microtime(time: Long) = new IdmlFunction0 {
    override protected def apply(cursor: IdmlValue): IdmlValue = IdmlValue(time * 1000)
    override def name: String                                  = "microtime"
  }
}
