package io.idml.datanodes

import org.scalatest._

/** Test the behaviour of the PBool class */
class PBoolTest extends FunSuite with MustMatchers {
  // Equality
  test("true == true")(PTrue must equal(PTrue))
  test("false == false")(PFalse must equal(PFalse))
  test("true != false")(PFalse must not equal PTrue)
  test("false != true")(PFalse must not equal PTrue)

  // bool
  test("true.bool() == true")(PTrue.bool() must equal(PTrue))
  test("false.bool() == false")(PFalse.bool() must equal(PFalse))
}
