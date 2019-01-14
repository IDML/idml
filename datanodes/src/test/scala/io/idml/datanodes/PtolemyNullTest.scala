package io.idml.datanodes

import io.idml.{PtolemyJson, PtolemyNull}
import org.scalatest._

/** Test the functionality of the PtolemyNull object */
class PtolemyNullTest extends FunSuite with MustMatchers {
  import PtolemyJson._

  // Parsing
  test("parse(null)")(PtolemyJson.parse("null") must equal(PtolemyNull))

  // Generation
  test("generate(null)")("null" must equal(compact(PtolemyNull)))

  // Equality
  test("null == null")(PtolemyNull must equal(PtolemyNull))
}
