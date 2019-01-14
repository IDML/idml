package io.idml.datanodes.regex

import java.util.regex.Pattern

import scala.collection.mutable.ListBuffer

/**
  * Implementation of PRegexLike for the standard Java regular expressions
  * @param regex
  */
class PJavaRegex(regex: String) extends PRegexLike(regex) {
  val pattern = Pattern.compile(regex)

  override def matches(target: String): List[List[String]] = {
    val r = pattern.matcher(target)
    val builder = ListBuffer.empty[List[String]]
    while (r.find()) {
      val result = (1 to r.groupCount()).toList.map { i =>
        r.group(i)
      }
      builder.append(result)
    }
    builder.toList
  }
  // scalastyle:off method.name
  override def `match`(target: String): List[String] = {
    val r = pattern.matcher(target)
    if (r.matches()) {
      val results = 1 to r.groupCount() map { i =>
        r.group(i)
      }
      results.toList
    } else {
      List[String]()
    }
  }
  // scalastyle:on method.name

  override def replace(target: String, replacement: String): String = {
    target.replaceAll(pattern.pattern(), replacement)
  }

  override def isMatch(target: String): Boolean = {
    pattern.matcher(target).matches()
  }

  override def split(target: String): List[String] = {
    pattern.split(target).toList
  }
}
