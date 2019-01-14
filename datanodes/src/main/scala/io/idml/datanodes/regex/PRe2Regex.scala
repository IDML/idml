package io.idml.datanodes.regex
import com.google.re2j.Pattern

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

class PRe2Regex(regex: String) extends PRegexLike(regex) {
  private val pattern = Pattern.compile(regex)

  override def matches(target: String): List[List[String]] = {
    val r       = pattern.matcher(target)
    val builder = ListBuffer.empty[List[String]]
    while (r.find()) {
      val result = (1 to r.groupCount()).toList.map { i =>
        r.group(i)
      }
      builder.append(result)
    }
    builder.toList
  }
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
  override def split(target: String): List[String]                  = pattern.split(target).toList
  override def isMatch(target: String): Boolean                     = pattern.matches(target)
  override def replace(target: String, replacement: String): String = pattern.matcher(target).replaceAll(replacement)
}
