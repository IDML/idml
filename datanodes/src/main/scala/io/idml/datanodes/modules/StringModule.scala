package io.idml.datanodes.modules

import io.idml.datanodes.IString
import io.idml._
import com.google.common.base.Charsets
import com.google.common.hash.Hashing

import scala.util.Try

/** Adds string node functions */
trait StringModule {
  this: IdmlValue =>

  /** Transform this node to a string */
  def string(): IdmlValue = this match {
    case _: IdmlString | _: IdmlNothing => this
    case n: IdmlInt                     => IdmlValue(n.value.toString)
    case n: IdmlDouble                  => IdmlValue(n.value.toString)
    case _                              => CastUnsupported
  }

  /** Make a string lowercase */
  def lowercase(): IdmlValue = this

  /** Make a string uppercase */
  def uppercase(): IdmlValue = this

  def capitalize(): IdmlValue = this

  def strip(): IdmlValue = this

  /** Format strings */
  def format(varargs: IdmlValue*): IdmlValue = {
    try {
      val args = varargs.map {
        case s: IdmlString => s.value
        case i: IdmlInt    => i.value
        case d: IdmlDouble => d.value
        case b: IdmlBool   => b.value
        case e: Any        => e
      }

      // don't run if anything is missing
      if (args.exists(_.isInstanceOf[IdmlNothing])) {
        InvalidParameters
      } else {
        this match {
          case s: IdmlString => IString(s.value.format(args: _*))
          case _             => InvalidCaller
        }
      }
    } catch {
      case e: java.util.MissingFormatArgumentException => InvalidParameters
    }
  }

}
