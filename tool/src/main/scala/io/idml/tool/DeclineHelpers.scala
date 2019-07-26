package io.idml.tool

import java.io.File

import cats.data.{NonEmptyList, Validated, ValidatedNel}
import com.monovore.decline.Argument

object DeclineHelpers {

  implicit val readFile: Argument[File] = new Argument[File] {

    override def read(string: String): ValidatedNel[String, File] =
      try {
        Validated
          .valid(new File(string))
          .ensure(NonEmptyList.of(s"Invalid File: $string does not exist."))(_.exists())
          .ensure(NonEmptyList.of(s"Invalid File: $string cannot be read."))(_.canRead())
      } catch { case npe: NullPointerException => Validated.invalidNel(s"Invalid File: $string (${npe.getMessage})") }

    override def defaultMetavar: String = "file"
  }

}
