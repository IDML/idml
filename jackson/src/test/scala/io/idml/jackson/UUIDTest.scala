package io.idml.jackson

import io.idml.{IdmlContext, IdmlValue}
import io.idml.datanodes.{PObject, PString}
import org.scalatest.{FunSuite, MustMatchers}

class UUIDTest extends FunSuite with MustMatchers {
  val funcs = new JacksonFunctions
  import funcs.uuid._

  def v3(pv: IdmlValue): IdmlValue = uuid3Function.eval(new IdmlContext(), pv)
  def v5(pv: IdmlValue): IdmlValue = uuid5Function.eval(new IdmlContext(), pv)

  /*
  I generated these using the python uuid3 and uuid5 methods to make sure they're the same
   */
  test("uuid3")(v3(PString("helloworld")) must equal(PString("fc5e038d-38a5-3032-8854-41e7fe7010b0")))
  test("uuid5")(v5(PString("helloworld")) must equal(PString("6adfb183-a4a2-594a-af92-dab5ade762a4")))
  test("uuid3 on an object")(v3(PObject()) must equal(PString("99914b93-2bd3-3a50-b983-c5e7c90ae93b")))
  test("uuid5 on an object")(v5(PObject()) must equal(PString("bf21a9e8-fbc5-5384-afb0-5b4fa0859e09")))

}
