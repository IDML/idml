package io.idml

import java.io.{File, IOException}
import java.nio.charset.Charset

import com.fasterxml.jackson.core.JsonParseException
import com.google.common.io.Files
import io.idml.jackson.IdmlJackson

import scala.collection.JavaConverters._
import scala.io.Source

/** A class capable of executing ptolemy json tests. It's intended that you'll integrate this with
  * your test framework.
  */
trait IdmlTestHarness {

  /** Add unmapped fields to the output? Set this to true if you do
    */
  protected val findUnmappedFields: Boolean = false

  /** The Idml instance used for parsing
    */
  protected val ptolemy: Idml = {
    if (findUnmappedFields) {
      Idml.autoBuilder().withListener(new UnmappedFieldsFinder).build()
    } else {
      Idml.autoBuilder().build()
    }
  }

  /** The default test name
    */
  protected val defaultName: String = "unnamed"

  /** String constants
    */
  val Mapping = "mapping"

  val Chain = "chain"

  val Name = "name"

  val Input = "input"

  val Output = "output"

  val Pending = "pending"

  /** Run all the suites in a directory
    */
  def executeResourceDirectory(
      path: String,
      extension: String = ".json",
      required: Boolean = true): Unit = {
    val files = findResourcesInDirectory(path, extension)
    if (required && files.isEmpty) {
      throw new IllegalArgumentException(
        s"Expected at least one file in $path with extension $extension but found none")
    }
    files.foreach(executeFile)
  }

  /** Run a single resource as a suite
    */
  def executeFile(path: File): Unit = {
    try {
      IdmlJackson.default.parse(Files.toString(path, Charset.defaultCharset())) match {
        case tests: IdmlArray  =>
          executeSuite(tests)
        case suite: IdmlObject =>
          executeSuite(suite)
        case other: Any        =>
          // scalastyle:off multiple.string.literals
          throw new IllegalArgumentException(
            s"Test suite must contain either an object or array of tests. Found ${other.getClass}")
        // scalastyle:on multiple.string.literals
      }
    } catch {
      case ex: JsonParseException =>
        throw new IllegalArgumentException(s"Failed to parse suite $path", ex)
      case ex: IOException        =>
        throw new IllegalArgumentException(s"Failed to open suite $path", ex)
    }
  }

  /** Execute the a suite
    */
  def executeSuite(suite: IdmlObject): Unit = {
    val mapping = suite.get(Mapping)
    val chain   = suite.get(Chain)

    val tests = suite.get("tests") match {
      case array: IdmlArray =>
        array
      case other: Any       =>
        throw new IllegalArgumentException(s"Tests must be objects. Found ${other.getClass}")
    }

    (mapping, chain) match {
      case (string: IdmlString, chain: IdmlNothing) =>
        executeSharedMappingSuite(tests, string.toStringValue)
      case (string: IdmlNothing, chain: IdmlArray)  =>
        val mappings = chain.items.flatMap(_.toStringOption)
        if (mappings.size != chain.items.size) {
          throw new IllegalArgumentException("Expected the chain to be an array of strings")
        }
        executeSharedChainSuite(tests, mappings.toSeq)
      case (_: IdmlNothing, _: IdmlNothing)         =>
        executeSuite(tests)
    }

  }

  /** Execute a mapping for a whole suite of tests
    */
  protected def executeSharedMappingSuite(tests: IdmlArray, mapping: String): Unit = {
    tests.items.foreach {
      case test: IdmlObject =>
        val name     = test.get(Name).toStringOption.getOrElse(defaultName)
        val input    = test.get(Input)
        val expected = test.get(Output)

        if (test.get(Pending).toBoolValue) {
          pendingTest(name)
        } else if (!test
            .get(Mapping)
            .isNothingValue || !test.get(Chain).isNothingValue) {
          throw new IllegalArgumentException(
            "You can't place a mapping or chain in the suite and the tests")
        } else {
          executeMappingTest(name, mapping, input, expected)
        }
      case other: Any       =>
        throw new IllegalArgumentException(s"Tests must be objects. Found ${other.getClass}")
    }
  }

  /** Execute a chain for a whole suite of tests
    */
  protected def executeSharedChainSuite(tests: IdmlArray, chain: Seq[String]): Unit = {
    tests.items.foreach {
      case test: IdmlObject =>
        val name     = test.get(Name).toStringOption.getOrElse(defaultName)
        val input    = test.get(Input)
        val expected = test.get(Output)

        if (!test.get(Pending).isNothingValue) {
          pendingTest(name)
        } else if (!test
            .get(Mapping)
            .isNothingValue || !test.get(Chain).isNothingValue) {
          throw new IllegalArgumentException(
            "You can't place a mapping or chain in the suite and the tests")
        } else {
          executeChainTest(name, chain, input, expected)
        }
      case other: Any       =>
        throw new IllegalArgumentException(s"Tests must be objects. Found ${other.getClass}")
    }
  }

  /** Execute the tests in a suite
    */
  def executeSuite(tests: IdmlArray): Unit = {
    tests.items.foreach {
      case test: IdmlObject =>
        executeTest(test)
      case other: Any       =>
        throw new IllegalArgumentException(s"Tests must be objects. Found ${other.getClass}")
    }
  }

  /** Run a test
    */
  def executeTest(test: IdmlObject): Unit = {
    val name = test.get(Name).toStringOption.getOrElse(defaultName)
    executeTest(name, test)
  }

  /** A test is marked as pending
    */
  protected def pendingTest(name: String): Unit = {}

  /** Run a test
    */
  protected def executeTest(name: String, test: IdmlObject): Unit = {
    val mapping  = test.get(Mapping)
    val chain    = test.get(Chain)
    val input    = test.get(Input)
    val expected = test.get(Output)

    if (!test.get(Pending).isNothingValue) {
      pendingTest(name)
    } else {
      (mapping, chain) match {
        case (string: IdmlString, chain: IdmlNothing) =>
          executeMappingTest(name, string.toStringValue, input, expected)

        case (string: IdmlNothing, chain: IdmlArray) =>
          val mappings = chain.items.flatMap(_.toStringOption)
          if (mappings.size != chain.items.size) {
            throw new IllegalArgumentException("Expected the chain to be an array of strings")
          }
          executeChainTest(name, mappings.toSeq, input, expected)
        case (l, r)                                  =>
          throw new IllegalArgumentException(
            s"The test must contain either a mapping string or chain array, mapping was ${l.getClass} and chain was ${r.getClass}"
          )
      }
    }
  }

  /** Run a mapping test
    *
    * @param name
    *   The name of the test
    * @param mapping
    *   The mapping text
    * @param input
    *   The input data
    * @param expected
    *   The expected output data
    */
  protected def executeMappingTest(
      name: String,
      mapping: String,
      input: IdmlValue,
      expected: IdmlValue): Unit = {
    executeMappingTest(name, loadMapping(mapping), input, expected)
  }

  /** Run a chain test
    *
    * @param name
    *   The name of the test
    * @param mappings
    *   The list of mapping text
    * @param input
    *   The input data
    * @param expected
    *   The expected output data
    */
  protected def executeChainTest(
      name: String,
      mappings: Seq[String],
      input: IdmlValue,
      expected: IdmlValue): Unit = {
    executeChainTest(name, ptolemy.chain(mappings.map(loadMapping): _*), input, expected)
  }

  /** Run a mapping test
    *
    * @param name
    *   The name of the test
    * @param mapping
    *   The parsed mapping
    * @param input
    *   The input data
    * @param expected
    *   The expected output data
    */
  protected def executeMappingTest(
      name: String,
      mapping: Mapping,
      input: IdmlValue,
      expected: IdmlValue): Unit = {
    val actual = mapping.run(input)
    if (!expected.isNothingValue) {
      compareResults(expected, actual)
    }
  }

  /** Run a chain test
    *
    * @param name
    *   The name of the test
    * @param chain
    *   The parsed chain
    * @param input
    *   The input data
    * @param expected
    *   The expected output data
    */
  protected def executeChainTest(
      name: String,
      chain: Mapping,
      input: IdmlValue,
      expected: IdmlValue): Unit = {
    val actual = chain.run(input)
    if (!expected.isNothingValue) {
      compareResults(expected, actual)
    }
  }

  /** The data compare function. Causes the test to fail if values don't match
    *
    * @param expected
    *   The expected results
    * @param actual
    *   The actual results
    */
  protected def compareResults(expected: IdmlValue, actual: IdmlValue): Unit = {
    if (expected != actual) {
      throw new RuntimeException(s"Test failed: $actual did not equal $expected")
    }
  }

  /** Find all the resources in a particular directory.
    *
    * @param path
    *   The path to the directory we want to check
    * @param extension
    *   Provide an extension to narrow the results down
    * @return
    *   A list of resources
    */
  protected def findResourcesInDirectory(path: String, extension: String): Array[File] = {
    val file = new File(getClass.getProtectionDomain.getCodeSource.getLocation.getPath)
    require(!file.isFile, "Not running as a jar")
    val url  = getClass.getResource("/" + path)
    // scalastyle:off null
    require(url != null, "Failed to find " + path)
    // scalastyle:on null

    val apps = new File(url.toURI)
    for (app <- apps.listFiles() if app.getName.endsWith(extension)) yield app
  }

  /** Load a mapping
    *
    * @param str
    *   The mapping.. either "@/a/path" or some idml
    * @return
    *   A mapping
    */
  protected def loadMapping(str: String): Mapping = {
    if (str.startsWith("@")) {
      ptolemy.compile(Source.fromInputStream(getClass.getResourceAsStream(str.tail)).mkString)
    } else {
      ptolemy.compile(str)
    }
  }
}
