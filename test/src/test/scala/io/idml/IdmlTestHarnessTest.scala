package io.idml

import io.idml.datanodes._
import io.idml.lang.DocumentParseException
import org.scalatest.FunSuite

/** Verifies the test harness */
class IdmlTestHarnessTest extends FunSuite {

  /** Counts the number of tests we find */
  class TestCounter extends IdmlTestHarness {
    var testsRun: Int = 0
    override def executeTest(name: String, test: IdmlObject): Unit = {
      testsRun += 1
    }
  }

  /** A default implementation of the harness for testing */
  class TestImpl extends IdmlTestHarness

  test("fails if the resource is not a json object") {
    intercept[IllegalArgumentException](new TestImpl().executeResourceDirectory("mock_tests", extension = "missing_object.json"))
  }

  test("fails if the resource does not contain a test array") {
    intercept[IllegalArgumentException](new TestImpl().executeResourceDirectory("mock_tests", extension = "missing_tests.json"))
  }

  test("can resolve a single suite from a resource folder") {
    val harness = new TestCounter()
    harness.executeResourceDirectory("mock_tests", extension = "single.json")
    assert(harness.testsRun === 2)
  }

  test("can resolve multiple suites from a resource folder") {
    val harness = new TestCounter()
    harness.executeResourceDirectory("mock_tests", extension = "many.json")
    assert(harness.testsRun === 4)
  }

  test("does nothing if the input and output match") {
    new TestImpl().executeSuite(
      PObject(
        "tests" -> PArray(
          PObject(
            "input"   -> PObject("b" -> PTrue),
            "output"  -> PObject("a" -> PTrue),
            "mapping" -> PString("a = b")
          )
        )
      )
    )
  }

  test("fails if the input and output do not match") {
    intercept[RuntimeException] {
      new TestImpl().executeSuite(
        PObject(
          "tests" -> PArray(
            PObject(
              "input"   -> PObject("b" -> PFalse),
              "output"  -> PObject("a" -> PTrue),
              "mapping" -> PString("a = b")
            )
          )
        )
      )
    }
  }

  test("fails if there's a parse exception") {
    intercept[DocumentParseException] {
      new TestImpl().executeSuite(
        PObject(
          "tests" -> PArray(
            PObject(
              "input"   -> PObject("b" -> PTrue),
              "output"  -> PObject("a" -> PTrue),
              "mapping" -> PString("syntax error :O")
            )
          )
        )
      )
    }
  }

  test("dynamically loads mappings from resources") {
    new TestImpl().executeSuite(
      PObject(
        "tests" -> PArray(
          PObject(
            "input"   -> PObject("b" -> PTrue),
            "output"  -> PObject("a" -> PTrue),
            "mapping" -> PString("@/mock_tests/load_me.ini")
          )
        )
      )
    )
  }

  test("can share the mapping in a suite between all tests in the file") {
    new TestImpl()
      .executeResourceDirectory("mock_tests", extension = "shared_mapping.json")
  }

  test("can share the chain in a suite between all tests in the file") {
    new TestImpl()
      .executeResourceDirectory("mock_tests", extension = "shared_chain.json")
  }

  test("can skip tests with chains") {
    new TestImpl()
      .executeResourceDirectory("mock_tests", extension = "chain_pending.json")
  }

  test("can skip tests with mappings") {
    new TestImpl().executeResourceDirectory("mock_tests", extension = "mapping_pending.json")
  }

}
