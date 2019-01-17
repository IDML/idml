import java.io.File

import cats._
import cats.effect._
import cats.implicits._
import cats.syntax._
import cats.data._
import com.monovore.decline._

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
