package io.idml.test.diffable
import io.circe.Json.Folder
import io.circe.{Json, JsonNumber, JsonObject}
import cats._
import cats.implicits._
import fs2._

import scala.util.Try

object DiffablePrinter {

  sealed trait PrinterAction
  case object Indent          extends PrinterAction
  case object Unindent        extends PrinterAction
  case class Print(s: String) extends PrinterAction
  case object Newline         extends PrinterAction
  case object Trim            extends PrinterAction

  case class Interpreter(indent: Int = 0, buffer: String = "")

  def go(events: List[PrinterAction]) = {
    events.foldLeft(new Interpreter) {
      case (interpreter, action) =>
        action match {
          case Indent   => interpreter.copy(indent = interpreter.indent + 1)
          case Unindent => interpreter.copy(indent = interpreter.indent - 1)
          case Print(s) => interpreter.copy(buffer = interpreter.buffer + s)
          case Newline =>
            interpreter.copy(buffer = interpreter.buffer + "\n" + ("  " * interpreter.indent))
          case Trim => interpreter.copy(buffer = interpreter.buffer.trim)
        }
    }
  }

  val folder: Folder[List[PrinterAction]] =
    new Folder[List[PrinterAction]] {
      override def onNull: List[PrinterAction] = List(Print("null"))
      override def onBoolean(value: Boolean): List[PrinterAction] =
        List(Print(value match {
          case true  => "true"
          case false => "false"
        }))
      override def onNumber(value: JsonNumber): List[PrinterAction] =
        List(Print(value.toString))
      override def onString(value: String): List[PrinterAction] =
        List(Print("\"" + value + "\""))
      override def onArray(value: Vector[Json]): List[PrinterAction] =
        List(
          List(
            Print("["),
            Indent,
            Newline
          ),
          value.flatMap(
            _.foldWith(folder) ++ List(Print(","), Newline)
          ),
          List(
            Trim,
            Unindent,
            Newline,
            Print("]")
          )
        ).flatten
      override def onObject(value: JsonObject): List[PrinterAction] =
        List(
          List(Print("{"), Indent, Newline),
          value.toList.sortBy(_._1).flatMap {
            case (k, v) =>
              List(
                Print(s""""$k":"""),
                Indent,
                Newline
              ) ++
                v.foldWith(folder) ++
                List(
                  Print(","),
                  Unindent,
                  Newline
                )
          },
          List(
            Trim,
            Unindent,
            Newline,
            Print("}")
          )
        ).flatten
    }

  def fold(j: Json) = j.foldWith(folder)

  def print(j: Json): Either[Throwable, String] =
    Try { go(fold(j)).buffer }.toEither.leftMap(_ => new Throwable("Unable to diff that JSON"))

}
