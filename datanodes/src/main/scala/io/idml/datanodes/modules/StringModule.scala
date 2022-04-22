package io.idml.datanodes.modules

import java.net.{URLDecoder, URLEncoder}
import java.nio.charset.StandardCharsets
import java.text.Normalizer
import java.util.Base64

import io.idml.datanodes.IString
import io.idml._
import com.google.common.base.Charsets
import com.google.common.hash.Hashing

import scala.util.Try

/** Adds string node functions */
trait StringModule {
  this: IdmlValue =>

  /** Transform this node to a string */
  def string(): IdmlValue =
    this match {
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

  def normalize(form: IdmlValue): IdmlValue =
    (this, form) match {
      case (input: IdmlString, form: IdmlString) =>
        StringModule.normalize(input.value, form.value)
      case _                                     => InvalidCaller
    }
  import StringModule.stringTransformer

  def base64encode(): IdmlValue        = stringTransformer(this)(StringModule.base64encode)
  def base64decode(): IdmlValue        = stringTransformer(this)(StringModule.base64decode)
  def base64mimeEncode(): IdmlValue    = stringTransformer(this)(StringModule.base64mimeEncode)
  def base64mimeDecode(): IdmlValue    = stringTransformer(this)(StringModule.base64mimeDecode)
  def base64urlsafeEncode(): IdmlValue = stringTransformer(this)(StringModule.base64urlsafeEncode)
  def base64urlsafeDecode(): IdmlValue = stringTransformer(this)(StringModule.base64urlsafeDecode)
  def urlEncode(): IdmlValue           = stringTransformer(this)(StringModule.urlEncode)
  def urlDecode(): IdmlValue           = stringTransformer(this)(StringModule.urlDecode)
}

object StringModule {
  def normalize(input: String, form: String): IdmlValue =
    Try { Normalizer.Form.valueOf(form) }
      .flatMap { form =>
        Try { Normalizer.normalize(input, form) }.map(IString)
      }
      .getOrElse(CastFailed)

  def stringTransformer(i: IdmlValue)(f: String => Option[String]): IdmlValue =
    i match {
      case s: IdmlString => f(s.value).map(IString).getOrElse(CastFailed)
      case _             => CastUnsupported
    }

  def base64encode(s: String): Option[String]        =
    Try { new String(Base64.getEncoder.encode(s.getBytes(StandardCharsets.UTF_8))) }.toOption
  def base64decode(s: String): Option[String]        =
    Try { new String(Base64.getDecoder.decode(s)) }.toOption
  def base64mimeEncode(s: String): Option[String]    =
    Try { Base64.getMimeEncoder.encodeToString(s.getBytes(StandardCharsets.UTF_8)) }.toOption
  def base64mimeDecode(s: String): Option[String]    =
    Try { new String(Base64.getMimeDecoder.decode(s)) }.toOption
  def base64urlsafeEncode(s: String): Option[String] =
    Try {
      new String(Base64.getUrlEncoder.encodeToString(s.getBytes(StandardCharsets.UTF_8)))
    }.toOption
  def base64urlsafeDecode(s: String): Option[String] =
    Try { new String(Base64.getUrlDecoder.decode(s)) }.toOption
  def urlEncode(s: String): Option[String]           =
    Try { URLEncoder.encode(s, StandardCharsets.UTF_8.name) }.toOption
  def urlDecode(s: String): Option[String]           =
    Try { URLDecoder.decode(s, StandardCharsets.UTF_8.name) }.toOption
}
