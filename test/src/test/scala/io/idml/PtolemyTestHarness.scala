package io.idml

import java.io.{File, IOException}
import java.nio.charset.Charset

import com.fasterxml.jackson.core.JsonParseException
import com.google.common.io.Files
import io.idml.jackson.PtolemyJackson

import scala.collection.JavaConverters._

/**
  * A class capable of executing ptolemy json tests. It's intended that you'll integrate this with your test framework.
  */
trait PtolemyTestHarness {

  /**
    * Add unmapped fields to the output? Set this to true if you do
    */
  protected val findUnmappedFields: Boolean = false

  /**
    * The Ptolemy instance used for parsing
    */
  protected val ptolemy: Ptolemy = {
    if (findUnmappedFields) {
      new Ptolemy(new PtolemyConf, List[PtolemyListener](new UnmappedFieldsFinder).asJava)
    } else {
      new Ptolemy()
    }
  }

  /**
    * Resolve resources
    */
  protected val resourceResolver: ResourceResolver = new ResourceResolver

  /**
    * The default test name
    */
  protected val defaultName: String = "unnamed"

  /**
    * String constants
    */
  val Mapping = "mapping"

  val Chain = "chain"

  val Name = "name"

  val Input = "input"

  val Output = "output"

  val Pending = "pending"

  /**
    * Run all the suites in a directory
    */
  def executeResourceDirectory(path: String, extension: String = ".json", required: Boolean = true): Unit = {
    val files = findResourcesInDirectory(path, extension)
    if (required && files.isEmpty) {
      throw new IllegalArgumentException(s"Expected at least one file in $path with extension $extension but found none")
    }
    files.foreach(executeFile)
  }

  /**
    * Run a single resource as a suite
    */
  def executeFile(path: File): Unit = {
    try {
      PtolemyJackson.default.parse(Files.toString(path, Charset.defaultCharset())) match {
        case tests: PtolemyArray =>
          executeSuite(tests)
        case suite: PtolemyObject =>
          executeSuite(suite)
        case other: Any =>
          // scalastyle:off multiple.string.literals
          throw new IllegalArgumentException(s"Test suite must contain either an object or array of tests. Found ${other.getClass}")
        // scalastyle:on multiple.string.literals
      }
    } catch {
      case ex: JsonParseException =>
        throw new IllegalArgumentException(s"Failed to parse suite $path", ex)
      case ex: IOException =>
        throw new IllegalArgumentException(s"Failed to open suite $path", ex)
    }
  }

  /**
    * Execute the a suite
    */
  def executeSuite(suite: PtolemyObject): Unit = {
    val mapping = suite.get(Mapping)
    val chain   = suite.get(Chain)

    val tests = suite.get("tests") match {
      case array: PtolemyArray =>
        array
      case other: Any =>
        throw new IllegalArgumentException(s"Tests must be objects. Found ${other.getClass}")
    }

    (mapping, chain) match {
      case (string: PtolemyString, chain: PtolemyNothing) =>
        executeSharedMappingSuite(tests, string.toStringValue)
      case (string: PtolemyNothing, chain: PtolemyArray) =>
        val mappings = chain.items.flatMap(_.toStringOption)
        if (mappings.size != chain.items.size) {
          throw new IllegalArgumentException("Expected the chain to be an array of strings")
        }
        executeSharedChainSuite(tests, mappings)
      case (_: PtolemyNothing, _: PtolemyNothing) =>
        executeSuite(tests)
    }

  }

  /**
    * Execute a mapping for a whole suite of tests
    */
  protected def executeSharedMappingSuite(tests: PtolemyArray, mapping: String): Unit = {
    tests.items.foreach {
      case test: PtolemyObject =>
        val name     = test.get(Name).toStringOption.getOrElse(defaultName)
        val input    = test.get(Input)
        val expected = test.get(Output)

        if (test.get(Pending).toBoolValue) {
          pendingTest(name)
        } else if (!test
                     .get(Mapping)
                     .isNothingValue || !test.get(Chain).isNothingValue) {
          throw new IllegalArgumentException("You can't place a mapping or chain in the suite and the tests")
        } else {
          executeMappingTest(name, mapping, input, expected)
        }
      case other: Any =>
        throw new IllegalArgumentException(s"Tests must be objects. Found ${other.getClass}")
    }
  }

  /**
    * Execute a chain for a whole suite of tests
    */
  protected def executeSharedChainSuite(tests: PtolemyArray, chain: Seq[String]): Unit = {
    tests.items.foreach {
      case test: PtolemyObject =>
        val name     = test.get(Name).toStringOption.getOrElse(defaultName)
        val input    = test.get(Input)
        val expected = test.get(Output)

        if (!test.get(Pending).isNothingValue) {
          pendingTest(name)
        } else if (!test
                     .get(Mapping)
                     .isNothingValue || !test.get(Chain).isNothingValue) {
          throw new IllegalArgumentException("You can't place a mapping or chain in the suite and the tests")
        } else {
          executeChainTest(name, chain, input, expected)
        }
      case other: Any =>
        throw new IllegalArgumentException(s"Tests must be objects. Found ${other.getClass}")
    }
  }

  /**
    * Execute the tests in a suite
    */
  def executeSuite(tests: PtolemyArray): Unit = {
    tests.items.foreach {
      case test: PtolemyObject =>
        executeTest(test)
      case other: Any =>
        throw new IllegalArgumentException(s"Tests must be objects. Found ${other.getClass}")
    }
  }

  /**
    * Run a test
    */
  def executeTest(test: PtolemyObject): Unit = {
    val name = test.get(Name).toStringOption.getOrElse(defaultName)
    executeTest(name, test)
  }

  /**
    * A test is marked as pending
    */
  protected def pendingTest(name: String): Unit = {}

  /**
    * Run a test
    */
  protected def executeTest(name: String, test: PtolemyObject): Unit = {
    val mapping  = test.get(Mapping)
    val chain    = test.get(Chain)
    val input    = test.get(Input)
    val expected = test.get(Output)

    if (!test.get(Pending).isNothingValue) {
      pendingTest(name)
    } else {
      (mapping, chain) match {
        case (string: PtolemyString, chain: PtolemyNothing) =>
          executeMappingTest(name, string.toStringValue, input, expected)

        case (string: PtolemyNothing, chain: PtolemyArray) =>
          val mappings = chain.items.flatMap(_.toStringOption)
          if (mappings.size != chain.items.size) {
            throw new IllegalArgumentException("Expected the chain to be an array of strings")
          }
          executeChainTest(name, mappings, input, expected)
        case (l, r) =>
          throw new IllegalArgumentException(
            s"The test must contain either a mapping string or chain array, mapping was ${l.getClass} and chain was ${r.getClass}")
      }
    }
  }

  /**
    * Run a mapping test
    *
    * @param name The name of the test
    * @param mapping The mapping text
    * @param input The input data
    * @param expected The expected output data
    */
  protected def executeMappingTest(name: String, mapping: String, input: PtolemyValue, expected: PtolemyValue): Unit = {
    executeMappingTest(name, loadMapping(mapping), input, expected)
  }

  /**
    * Run a chain test
    *
    * @param name The name of the test
    * @param mappings The list of mapping text
    * @param input The input data
    * @param expected The expected output data
    */
  protected def executeChainTest(name: String, mappings: Seq[String], input: PtolemyValue, expected: PtolemyValue): Unit = {
    executeChainTest(name, ptolemy.newChain(mappings.map(loadMapping): _*), input, expected)
  }

  /**
    * Run a mapping test
    *
    * @param name The name of the test
    * @param mapping The parsed mapping
    * @param input The input data
    * @param expected The expected output data
    */
  protected def executeMappingTest(name: String, mapping: PtolemyMapping, input: PtolemyValue, expected: PtolemyValue): Unit = {
    val actual = mapping.run(input)
    if (!expected.isNothingValue) {
      compareResults(expected, actual)
    }
  }

  /**
    * Run a chain test
    *
    * @param name The name of the test
    * @param chain The parsed chain
    * @param input The input data
    * @param expected The expected output data
    */
  protected def executeChainTest(name: String, chain: PtolemyChain, input: PtolemyValue, expected: PtolemyValue): Unit = {
    val actual = chain.run(input)
    if (!expected.isNothingValue) {
      compareResults(expected, actual)
    }
  }

  /**
    * The data compare function. Causes the test to fail if values don't match
    *
    * @param expected The expected results
    * @param actual The actual results
    */
  protected def compareResults(expected: PtolemyValue, actual: PtolemyValue): Unit = {
    if (expected != actual) {
      throw new RuntimeException(s"Test failed: $actual did not equal $expected")
    }
  }

  /**
    * Find all the resources in a particular directory.
    *
    * @param path The path to the directory we want to check
    * @param extension Provide an extension to narrow the results down
    * @return A list of resources
    */
  protected def findResourcesInDirectory(path: String, extension: String): Array[File] = {
    val file = new File(getClass.getProtectionDomain.getCodeSource.getLocation.getPath)
    require(!file.isFile, "Not running as a jar")
    val url = getClass.getResource("/" + path)
    // scalastyle:off null
    require(url != null, "Failed to find " + path)
    // scalastyle:on null

    val apps = new File(url.toURI)
    for (app <- apps.listFiles() if app.getName.endsWith(extension)) yield app
  }

  /**
    * Load a mapping
    *
    * @param str The mapping.. either "@/a/path" or some idml
    * @return A mapping
    */
  protected def loadMapping(str: String): PtolemyMapping = {
    if (str.startsWith("@")) {
      ptolemy.fromResource(str.tail)
    } else {
      ptolemy.fromString(str)
    }
  }
}
