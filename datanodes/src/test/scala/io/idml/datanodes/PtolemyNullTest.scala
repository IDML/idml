package io.idml.datanodes

import io.idml.PtolemyNull
import org.scalatest._

/** Test the functionality of the PtolemyNull object */
class PtolemyNullTest extends FunSuite with MustMatchers {


  // Equality
  test("null == null")(PtolemyNull must equal(PtolemyNull))
}
