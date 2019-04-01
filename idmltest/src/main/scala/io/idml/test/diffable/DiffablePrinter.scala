package io.idml.test.diffable
import io.circe.Json.Folder
import io.circe.{Json, JsonNumber, JsonObject}
import cats._
import cats.implicits._
import fs2._

object DiffablePrinter {

  sealed trait PrinterAction
  case object Indent          extends PrinterAction
  case object Unindent        extends PrinterAction
  case class Print(s: String) extends PrinterAction
  case object Newline         extends PrinterAction
  case object Trim            extends PrinterAction

  case class Interpreter(indent: Int = 0, buffer: String = "")

  def go[Pure[_]](events: Stream[Pure, PrinterAction]) = {
    events.fold(new Interpreter) {
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

  def folder(): Folder[Stream[Pure, PrinterAction]] =
    new Folder[Stream[Pure, PrinterAction]] {
      override def onNull: Stream[Pure, PrinterAction] = Stream.emit(Print("null"))
      override def onBoolean(value: Boolean): Stream[Pure, PrinterAction] =
        Stream.emit(Print(value match {
          case true  => "true"
          case false => "false"
        }))
      override def onNumber(value: JsonNumber): Stream[Pure, PrinterAction] =
        Stream.emit(Print(value.toString))
      override def onString(value: String): Stream[Pure, PrinterAction] =
        Stream.emit(Print("\"" + value + "\""))
      override def onArray(value: Vector[Json]): Stream[Pure, PrinterAction] =
        List(
          Stream
            .emits(
              List(
                Print("["),
                Indent,
                Newline
              ))
            .covary[Pure],
          Stream
            .emits(value)
            .flatMap(
              _.foldWith(folder) ++ Stream.emit(Print(",")) ++ Stream.emit(Newline)
            ),
          Stream
            .emits(
              List(
                Trim,
                Unindent,
                Newline,
                Print("]")
              ))
            .covary[Pure],
        ).combineAll
      override def onObject(value: JsonObject): Stream[Pure, PrinterAction] =
        List(
          Stream.emits(List(Print("{"), Indent, Newline)),
          Stream.emits(value.toList.sortBy(_._1)).flatMap {
            case (k, v) =>
              List(
                Stream
                  .emits(
                    List(
                      Print(s""""$k":"""),
                      Indent,
                      Newline
                    ))
                  .covary[Pure],
                v.foldWith(folder),
                Stream
                  .emits(
                    List(
                      Print(","),
                      Unindent,
                      Newline
                    ))
                  .covary[Pure]
              ).combineAll
          },
          Stream.emits(
            List(
              Trim,
              Unindent,
              Newline,
              Print("}")
            )),
        ).combineAll
    }

  def fold(j: Json) = j.foldWith(folder)

  def print(j: Json): Either[Throwable, String] =
    go(fold(j)).toList.headOption.map(_.buffer).map(Right.apply).getOrElse(Left(new Throwable("Unable to diff that JSON")))

}
