package io.idml.datanodes.regex

abstract case class PRegexLike(regex: String) {
  // scalastyle:off method.name
  def `match`(target: String): List[String]
  // scalastyle:on method.name
  def split(target: String): List[String]
  def isMatch(target: String): Boolean
  def replace(target: String, replacement: String): String
  def matches(target: String): List[List[String]]
}

object PRegexFactory {
  var regexType: Option[String] = None

  def getRegex(i: String): PRegexLike = {
    regexType match {
      case Some("java") =>
        new PRe2Regex(i) // can re-add java later
      case Some("re2")  =>
        new PRe2Regex(i)
      case None         =>
        new PRe2Regex(i)
      case _            =>
        new PRe2Regex(i)
    }
  }
}
