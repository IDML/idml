package io.idml.test
import io.circe.Json
import io.circe.literal._
import io.idml.test.diffable.{DiffableParser, DiffablePrinter}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must

class DiffableFormatsSpec extends AnyFlatSpec with must.Matchers {

  "DiffPrinter" should "print things in a way that's nice to diff" in {
    DiffablePrinter.print(json"[1,2,3]") must equal(Right("""[
        |  1,
        |  2,
        |  3,
        |]""".stripMargin))
  }
  "DiffPrinter" should "sort keys" in {
    DiffablePrinter.print(
      Json.obj(
        "a" -> Json.fromInt(1),
        "z" -> Json.fromInt(26),
        "b" -> Json.fromInt(2)
      )) must equal(Right("""{
        |  "a":
        |    1,
        |  "b":
        |    2,
        |  "z":
        |    26,
        |}""".stripMargin))
  }
  "DiffPrinter and DiffParser" should "work together" in {
    val jsons = List(
      Json.arr((1 to 10).map(Json.fromInt): _*),
      Json.Null,
      Json.obj(
        "a" -> Json.fromString("blah")
      ),
      Json.obj(
        "a" -> Json.obj(
          "b" -> Json.obj(
            "blah" -> Json.arr(Json.fromString("hi"), Json.fromInt(123), Json.fromFloat(1.23f).get)
          )
        )
      )
    )
    import io.idml.test.diffable.TestDiff.RethrowableEither
    jsons.foreach { j =>
      DiffableParser.parse(DiffablePrinter.print(j).rethrow).option.get must equal(j)
    }
  }

}
