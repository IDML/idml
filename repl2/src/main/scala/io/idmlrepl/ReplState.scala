package io.idmlrepl

import io.idml.{IdmlMapping, IdmlObject}

sealed trait ReplMode
case class JSONMode(data: String = "") extends ReplMode
case class IDMLMode(data: String = "") extends ReplMode
case object QuitMode extends ReplMode

case class ReplState(mode: ReplState, data: IdmlObject, category: Option[IdmlMapping])