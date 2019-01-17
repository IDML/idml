package io.idml.utils

import io.idml.lang.DocumentParseException
import org.scalatest.FunSuite
import org.scalatest.MustMatchers

/** This class ensures we valiate IDML documents correctly */
class DocumentValidatorTest extends FunSuite with MustMatchers {

  test("Accepts documents that are valid mappings") {
    DocumentValidator.validate("p = q")
  }

  test("Accepts documents that are valid schemas") {
    DocumentValidator.validate("p : string()")
    DocumentValidator.validate("p : array(string())")
  }

  test("Rejects documents that are both schemas and mappings") {
    intercept[ClassificationException](DocumentValidator.validate("p = q \n r : string()"))
  }

  test("Rejects empty documents") {
    intercept[ClassificationException](DocumentValidator.validate(""))
  }

  test("Rejects documents that don't parse") {
    intercept[DocumentParseException](DocumentValidator.validate("abc"))
  }

  test("Rejects documents that have inappropriate functions in for their type") {
    intercept[ClassificationException](DocumentValidator.validate("foo : int().extract(bar)"))
    DocumentValidator.validate("foo = int().extract(bar)")
    intercept[ClassificationException](DocumentValidator.validate("foo = array(int())"))
    DocumentValidator.validate("foo : array(int())")
  }

  test("accepts complex filters") {
    val mapping =
      """# Comment
                    |[main]
                    |body = (
                    |        apply("post/delete") [ root.verb == "post" or root.verb == "delete" ] |
                    |        apply("share") [ root.verb == "share"].filter(root.verb == "share")
                    |    )
                    |deleted = true [root.verb == "delete"]""".stripMargin
    DocumentValidator.validate(mapping)
  }

  test("can validate maths") {
    DocumentValidator.validate("foo = 1 + 42")
  }

  test("accept mappings with variables in as valid") {
    DocumentValidator.validate("""let foo = 2 + 2
        |result = $foo
      """.stripMargin)
  }

  test("accept mappings with if expression as as valid") {
    DocumentValidator.validate("""result = if foo exists then 1 else 2
      """.stripMargin)
  }

  test("accept mappings with matches in as valid") {
    DocumentValidator.validate("result = match foo | a exists => 1 | b exists => 2")

  }

  test("accept mappings with array literal function calls as valid") {
    DocumentValidator.validate("result = [$a, 1, b].flatten()")
  }
}
