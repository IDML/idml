package io.idml.tutor

import fansi.Color._
import fansi.Underlined

object Colours {
  def cyan(s: String): String = Cyan(s).render
  def red(s: String): String = Red(s).render
  def green(s: String): String = Green(s).render
  def grey(s: String): String = DarkGray(s).render
  def yellow(s: String): String = Yellow(s).render
  def underlined(s: String): String = Underlined.On(s).render
}
