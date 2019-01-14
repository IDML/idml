package io.idml.datanodes.modules

import java.net.URL

import com.google.re2j.Pattern
import io.idml.datanodes.{PArray, PUrl}
import io.idml.{CastFailed, CastUnsupported, PtolemyNothing, PtolemyString, PtolemyValue}

import scala.collection.mutable
import scala.util.Try

object UrlModule {
  val regex =
    Pattern.compile("""\b((?:https?://|www\d{0,3}\.|[a-z0-9.\-]+\.[a-z]{2,4}/)(?:[^\p{Z}\s()<>]+|\(([^\p{Z}\s()<>]+|(\([^\p{Z}\s()<>]+\)))*\))+(?:\(([^\p{Z}\s()<>]+|(\([^\p{Z}\s()<>]+\)))*\)|[^\p{Z}\s!()\[\]{};:\'\".,<>?«»“”‘’]))""")
  def findAllIn(p: Pattern)(s: String): List[String] = {
    val m = p.matcher(s)
    val results = mutable.Buffer.empty[String]
    while (m.find()) {
      results.append(s.slice(m.start(), m.end()))
    }
    results.toList
  }
}

/** Adds URL manipulation behaviour to data nodes */
trait UrlModule {
  this: PtolemyValue =>

  import UrlModule._

  def urls(): PtolemyValue = this match {
    case s: PtolemyString =>
      new PArray(findAllIn(regex)(s.value).flatMap(u => Try(new PUrl(new URL(u))).toOption).toBuffer[PtolemyValue])
    case _ => CastUnsupported
  }

  /** Construct a new URL by parsing a string */
  def url(): PtolemyValue = this match {
    case _: PUrl | _: PtolemyNothing => this
    case n: PtolemyString =>
      Try(new URL(n.value)).map(new PUrl(_)).getOrElse(CastFailed)
    case _ => CastUnsupported
  }

}
