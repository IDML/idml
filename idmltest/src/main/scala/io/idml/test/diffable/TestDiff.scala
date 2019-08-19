package io.idml.test.diffable
import difflib.DiffUtils
import io.circe.Json
import cats._
import cats.implicits._
import cats.effect._

import scala.collection.JavaConverters._
import DiffablePrinter.print
import fansi.EscapeAttr
import fansi.Color._

object TestDiff {
  implicit class RethrowableEither[T](e: Either[Throwable, T]) {
    def rethrow: T = e match {
      case Left(t)  => throw t
      case Right(v) => v
    }
  }

  def generateDiff(got: Json, wanted: Json, context: Int = 2): String = {
    (print(got), print(wanted)).bisequence.map {
      case (gots, wanteds) =>
        val gotLines    = gots.linesWithSeparators.map(_.stripLineEnd).toList.asJava
        val wantedLines = wanteds.linesWithSeparators.map(_.stripLineEnd).toList.asJava
        val patch       = DiffUtils.diff(gotLines, wantedLines)
        val unified     = DiffUtils.generateUnifiedDiff("output.json", "expected-output.json", gotLines, patch, context)

        val colourer = colourUnifiedDiffLine(Red, Blue, Magenta)
        unified.asScala.map(colourer).mkString("\n")
    }.rethrow
  }

  def colourUnifiedDiffLine(old: EscapeAttr, expected: EscapeAttr, info: EscapeAttr): String => String = {
    (_: String) match {
      case s if s.startsWith("---") => old(s)
      case s if s.startsWith("+++") => expected(s)
      case s if s.startsWith("@@")  => info(s)
      case s if s.startsWith("- ")  => old(s)
      case s if s.startsWith("+ ")  => expected(s)
      case s                        => s
    }
  }.map(_.toString)

}
