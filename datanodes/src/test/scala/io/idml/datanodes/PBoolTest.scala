package io.idml.datanodes

import org.scalatest._

/** Test the behaviour of the PBool class */
class PBoolTest extends FunSuite with MustMatchers {
  // Equality
  test("true == true")(ITrue must equal(ITrue))
  test("false == false")(IFalse must equal(IFalse))
  test("true != false")(IFalse must not equal ITrue)
  test("false != true")(IFalse must not equal ITrue)

  // bool
  test("true.bool() == true")(ITrue.bool() must equal(ITrue))
  test("false.bool() == false")(IFalse.bool() must equal(IFalse))
}
