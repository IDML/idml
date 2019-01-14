package io.idml.datanodes

import io.idml.PtolemyJson
import org.scalatest._

/** Test the behaviour of the PBool class */
class PBoolTest extends FunSuite with MustMatchers {
  import PtolemyJson._

  // Parsing
  test("parse(true)")(PtolemyJson.parse("true") must equal(PTrue))
  test("parse(false)")(PtolemyJson.parse("false") must equal(PFalse))

  // Generation
  test("generate(true)")("true" must equal(compact(PTrue)))
  test("generate(false)")("false" must equal(compact(PFalse)))

  // Equality
  test("true == true")(PTrue must equal(PTrue))
  test("false == false")(PFalse must equal(PFalse))
  test("true != false")(PFalse must not equal PTrue)
  test("false != true")(PFalse must not equal PTrue)

  // bool
  test("true.bool() == true")(PTrue.bool() must equal(PTrue))
  test("false.bool() == false")(PFalse.bool() must equal(PFalse))
}
