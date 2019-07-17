package io.idml

import java.io.File

import io.idml.jackson.difftool.Diff
import org.scalatest.FreeSpec

/**
  * The base class for IdmlTestHarness when integrated with ScalaTest. The dependency is not transient!
  */
class IdmlScalaTestBase(directory: String, extension: String = "Suite.json", override val findUnmappedFields: Boolean = false)
    extends FreeSpec
    with IdmlTestHarness {

  // Run the tests in a particular directory
  executeResourceDirectory(directory, extension)

  /**
    * Execute a suite file as a ScalaTest test
    */
  override def executeFile(file: File): Unit = {
    file.getName - {
      super.executeFile(file)
    }
  }

  /**
    * Add test annotation to a mapping test
    */
  override protected def executeMappingTest(name: String, mapping: String, input: IdmlValue, expected: IdmlValue): Unit = {
    name in {
      super.executeMappingTest(name, mapping, input, expected)
    }
  }

  /**
    * Add test annotation to a chain test
    */
  override protected def executeChainTest(name: String, mappings: Seq[String], input: IdmlValue, expected: IdmlValue): Unit = {
    name in {
      super.executeChainTest(name, mappings, input, expected)
    }
  }

  /**
    * Use ScalaTest's own deep comparator and assertion
    */
  override def compareResults(expected: IdmlValue, actual: IdmlValue): Unit = {
    if (expected != actual) {
      fail(s"Results differ: ${Diff.pretty(expected, actual)}")
    }
  }

  protected override def pendingTest(name: String): Unit = {
    name in {
      pending
    }
  }

}
