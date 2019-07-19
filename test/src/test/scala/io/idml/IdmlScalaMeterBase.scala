package io.idml

import java.io.File

import org.scalameter.Gen
import org.scalameter.api._

/**
  * Links the IdmlTestHarness json syntax to ScalaMeter,
  */
class IdmlScalaMeterBase(directory: String,
                         extension: String = "Suite.json",
                         override val findUnmappedFields: Boolean = false,
                         val verifyResults: Boolean = false)
    extends PerformanceTest.Quickbenchmark
    with IdmlTestHarness {

  // Define the test range. In this case we don't have any input parameters
  protected val range = Gen.unit("none")

  // Run the tests in a particular directory
  executeResourceDirectory(directory, extension)

  /**
    * Execute a file as a benchmark
    */
  override def executeFile(file: File): Unit = {
    performance of file.getName in {
      super.executeFile(file)
    }
  }

  /**
    * Optionally compare results
    */
  protected override def compareResults(expected: IdmlValue, actual: IdmlValue): Unit = {
    if (verifyResults) {
      super.compareResults(expected, actual)
    }
  }

  /**
    * Execute a parsed mapping as a test
    */
  protected override def executeMappingTest(name: String, mapping: Mapping, input: IdmlValue, expected: IdmlValue): Unit = {
    measure method name in {
      using(range) in { i =>
        mapping.run(input)
      }
    }
  }

  /**
    * Execute a parsed chain as a test
    */
  protected override def executeChainTest(name: String, chain: Mapping, input: IdmlValue, expected: IdmlValue): Unit = {
    measure method name in {
      using(range) config (
        exec.benchRuns          -> 5,
        exec.independentSamples -> 5
      ) in { i =>
        for (_ <- 0 to 1000) {
          chain.run(input)
        }
      }
    }
  }

}
