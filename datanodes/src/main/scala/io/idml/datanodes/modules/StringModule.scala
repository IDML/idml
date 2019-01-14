package io.idml.datanodes.modules

import io.idml.datanodes.PString
import io.idml._
import com.google.common.base.Charsets
import com.google.common.hash.Hashing

import scala.util.Try

/** Adds string node functions */
trait StringModule {
  this: PtolemyValue =>

  /** Transform this node to a string */
  def string(): PtolemyValue = this match {
    case _: PtolemyString | _: PtolemyNothing => this
    case n: PtolemyInt                        => PtolemyValue(n.value.toString)
    case n: PtolemyDouble                     => PtolemyValue(n.value.toString)
    case _                                    => CastUnsupported
  }

  /** Make a string lowercase */
  def lowercase(): PtolemyValue = this

  /** Make a string uppercase */
  def uppercase(): PtolemyValue = this

  def capitalize(): PtolemyValue = this

  def strip(): PtolemyValue = this

  /** Format strings */
  def format(varargs: PtolemyValue*): PtolemyValue = {
    try {
      val args = varargs.map {
        case s: PtolemyString => s.value
        case i: PtolemyInt    => i.value
        case d: PtolemyDouble => d.value
        case b: PtolemyBool   => b.value
        case e: Any           => e
      }

      // don't run if anything is missing
      if (args.exists(_.isInstanceOf[PtolemyNothing])) {
        InvalidParameters
      } else {
        this match {
          case s: PtolemyString => PString(s.value.format(args: _*))
          case _                => InvalidCaller
        }
      }
    } catch {
      case e: java.util.MissingFormatArgumentException => InvalidParameters
    }
  }

}
