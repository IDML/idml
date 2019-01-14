package io.idml.datanodes

import io.idml.PtolemyJson
import org.scalatest._

class UUIDTest extends FunSuite with MustMatchers {

  /*
  I generated these using the python uuid3 and uuid5 methods to make sure they're the same
   */
  test("uuid3")(PString("helloworld").uuid3() must equal(PString("fc5e038d-38a5-3032-8854-41e7fe7010b0")))
  test("uuid5")(PString("helloworld").uuid5() must equal(PString("6adfb183-a4a2-594a-af92-dab5ade762a4")))
  test("uuid3 on an object")(PObject().uuid3() must equal(PString("99914b93-2bd3-3a50-b983-c5e7c90ae93b")))
  test("uuid5 on an object")(PObject().uuid5() must equal(PString("bf21a9e8-fbc5-5384-afb0-5b4fa0859e09")))

}
