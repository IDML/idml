package io.idml.test
import cats.effect.ExitCode
import cats._, cats.implicits._

sealed trait TestState
object TestState {
  def toExitCode(ts: TestState): ExitCode = ts match {
    case Error   => ExitCode.Error
    case Failed  => ExitCode.Error
    case Success => ExitCode.Success
    case Updated => ExitCode.Success
  }
  implicit val testStateMonoid: Monoid[TestState] = new Monoid[TestState] {
    override def empty: TestState = TestState.Success
    override def combine(x: TestState, y: TestState): TestState = Set(x, y) match {
      case tss if tss.contains(Error)   => Error
      case tss if tss.contains(Failed)  => Failed
      case tss if tss.contains(Updated) => Updated
      case tss if tss.contains(Success) => Success
    }
  }
  case object Error   extends TestState
  case object Failed  extends TestState
  case object Success extends TestState
  case object Updated extends TestState
}
