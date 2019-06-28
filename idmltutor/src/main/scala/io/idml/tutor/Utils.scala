package io.idml.tutor

import com.google.common.base.{CharMatcher, Strings}

object Utils {

  def center(s: String, width: Int, realLength: Int) = {
    val padSize = width - realLength
    if (padSize <= 0) {
      s
    } else {
      Strings.padEnd(
        Strings.padStart(s, realLength + padSize / 2, ' '),
        width,
        ' '
      )
    }
  }

}
