package io.idml.test
import cats.effect.ExitCode

sealed trait TestState
object TestState {
  def toExitCode(statuses: List[TestState]): ExitCode = {
    if (statuses.contains(Failed)) ExitCode.Error
    else if (statuses.contains(Failed)) ExitCode.Error
    else ExitCode.Success
  }
  case object Error   extends TestState
  case object Success extends TestState
  case object Failed  extends TestState
  case object Updated extends TestState
}
