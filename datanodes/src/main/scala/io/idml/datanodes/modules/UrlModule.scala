package io.idml.datanodes.modules

import java.net.URL

import com.google.re2j.Pattern
import io.idml.datanodes.{IArray, IUrl}
import io.idml.{CastFailed, CastUnsupported, IdmlNothing, IdmlString, IdmlValue}

import scala.collection.mutable
import scala.util.Try

object UrlModule {
  val regex                                          =
    Pattern.compile(
      """\b((?:https?://|www\d{0,3}\.|[a-z0-9.\-]+\.[a-z]{2,4}/)(?:[^\p{Z}\s()<>]+|\(([^\p{Z}\s()<>]+|(\([^\p{Z}\s()<>]+\)))*\))+(?:\(([^\p{Z}\s()<>]+|(\([^\p{Z}\s()<>]+\)))*\)|[^\p{Z}\s!()\[\]{};:\'\".,<>?«»“”‘’]))"""
    )
  def findAllIn(p: Pattern)(s: String): List[String] = {
    val m       = p.matcher(s)
    val results = mutable.Buffer.empty[String]
    while (m.find()) {
      results.append(s.slice(m.start(), m.end()))
    }
    results.toList
  }
}

/** Adds URL manipulation behaviour to data nodes */
trait UrlModule {
  this: IdmlValue =>

  import UrlModule._

  def urls(): IdmlValue =
    this match {
      case s: IdmlString =>
        new IArray(
          findAllIn(regex)(s.value)
            .flatMap(u => Try(new IUrl(new URL(u))).toOption)
            .toBuffer[IdmlValue])
      case _             => CastUnsupported
    }

  /** Construct a new URL by parsing a string */
  def url(): IdmlValue =
    this match {
      case _: IUrl | _: IdmlNothing => this
      case n: IdmlString            =>
        Try(new URL(n.value)).map(new IUrl(_)).getOrElse(CastFailed)
      case _                        => CastUnsupported
    }

}
