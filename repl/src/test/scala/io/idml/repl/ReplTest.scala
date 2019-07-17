package io.idmlrepl

import java.io.PrintWriter

import io.idml.jackson.PtolemyJackson
import io.idml.{jackson, _}
import org.scalatest.{BeforeAndAfterAll, FunSpec}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito.{never, stub, verify, when}
import org.mockito.Matchers.{any, anyString}

import scala.tools.jline.TerminalFactory

class ReplTest extends FunSpec with MockitoSugar with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    super.afterAll()
    TerminalFactory.get().restore()
  }

  trait mock

  class TestRepl extends Repl {
    override val out          = mock[PrintWriter]
    override val IDML_VERSION = ""
    override val BUILD_DATE   = ""
  }

  val VERSION_VALID      = "1.2.3"
  val IDML_VERSION_VALID = "4.5.10"
  val BUILD_DATE_VALID   = "201502201600"
  val UNKNOWN_PROPERTY   = "unknown"

  val INPUT_JSON         = "{}"
  val INPUT_IDML         = "test = test"
  val INPUT_SCHEMA       = "test = test"
  val INPUT_MODE_JSON    = "json"
  val INPUT_MODE_IDML    = "idml"
  val INPUT_MODE_SCHEMA  = "schema"
  val INPUT_MODE_INVALID = "inputmode"

  val PROCESS_INPUT_ERR_PREFIX  = "Error: Unknown mode ["
  val PROCESS_INPUT_ERR_POSTFIX = "]"

  val PROCESS_JSON_ACCEPTED     = "JSON accepted"
  val PROCESS_JSON_ERROR_PREFIX = "Error: "
  val PROCESS_JSON_EXCEPTION    = "json exception"

  val PROCESS_IDML_ERROR_PREFIX = "Error: "
  val PROCESS_IDML_EXCEPTION    = "idml exception"

  val PROCESS_SCHEMA_EXPECTED_MAPPING = "test = test\n"
  val PROCESS_SCHEMA_ERROR_PREFIX     = "Error: "
  val PROCESS_SCHEMA_EXCEPTION        = "schema-exception"

  val TOGGLE_UNMAPPED_ENABLED  = "Unmapped fields are enabled"
  val TOGGLE_UNMAPPED_DISABLED = "Unmapped fields are disabled"

  val LOAD_FILE_EXCEPTION       = "Error:  (No such file or directory)"
  val LOAD_FILE_EXPECTED_STRING = "{\n  \"test\": \"value\"\n}"

  describe("displaying the help text") {
    it("should print a string to the correct writer if called") {
      val mockRepl        = mock[Repl]
      val mockPrintWriter = mock[PrintWriter]
      stub(mockRepl.out).toReturn(mockPrintWriter)
      when(mockRepl.displayHelp()).thenCallRealMethod()
      mockRepl.displayHelp()
      verify(mockRepl.out).println(anyString())
    }
  }

  val ptolemy = new Ptolemy(
      new PtolemyConf,
      new FunctionResolverService()
    )

  describe("processing input based on string") {

    val mockPrintWriter = mock[PrintWriter]

    it("should pass the input on for json processing if mode is appropriate") {
      val mockRepl = mock[Repl]
      stub(mockRepl.out).toReturn(mockPrintWriter)
      when(mockRepl.processInput(any[Ptolemy]())(anyString())).thenCallRealMethod()
      stub(mockRepl.mode).toReturn(INPUT_MODE_JSON)
      mockRepl.processInput(ptolemy)(INPUT_JSON)
      verify(mockRepl).processJson(INPUT_JSON)
      verify(mockRepl, never()).processIdml(any[Ptolemy])(anyString())
      verify(mockRepl, never()).processSchema(any[Ptolemy])(anyString())
    }

    it("should pass the input on for IDML processing if mode is appropriate") {
      val mockRepl = mock[Repl]
      stub(mockRepl.out).toReturn(mockPrintWriter)
      when(mockRepl.processInput(any[Ptolemy])(anyString())).thenCallRealMethod()
      stub(mockRepl.mode).toReturn(INPUT_MODE_IDML)
      mockRepl.processInput(ptolemy)(INPUT_IDML)
      verify(mockRepl).processIdml(ptolemy)(INPUT_IDML)
      verify(mockRepl, never()).processJson(anyString())
      verify(mockRepl, never()).processSchema(any[Ptolemy])(anyString())
    }

    it("should pass the input on for schema processing if mode is appropriate") {
      val mockRepl = mock[Repl]
      stub(mockRepl.out).toReturn(mockPrintWriter)
      when(mockRepl.processInput(any[Ptolemy])(anyString())).thenCallRealMethod()
      stub(mockRepl.mode).toReturn(INPUT_MODE_SCHEMA)
      mockRepl.processInput(ptolemy)(INPUT_SCHEMA)
      verify(mockRepl).processSchema(ptolemy)(INPUT_SCHEMA)
      verify(mockRepl, never()).processIdml(any[Ptolemy])(anyString())
      verify(mockRepl, never()).processJson(anyString())
    }

    it("should print out an error if the mode does not match") {
      val mockRepl = mock[Repl]
      stub(mockRepl.out).toReturn(mockPrintWriter)
      when(mockRepl.processInput(any[Ptolemy])(anyString())).thenCallRealMethod()
      stub(mockRepl.mode).toReturn(INPUT_MODE_INVALID)
      mockRepl.processInput(ptolemy)(INPUT_JSON)
      verify(mockPrintWriter).println(
        PROCESS_INPUT_ERR_PREFIX
          + INPUT_MODE_INVALID
          + PROCESS_INPUT_ERR_POSTFIX)
    }
  }

  describe("processing of json input") {

    val repl = new TestRepl()

    it("should attempt to parse the input, print status and change mode to IDML if input is valid") {
      repl.processJson(INPUT_JSON)

      assert(
        repl.doc.get == PtolemyJackson.default
          .parse(INPUT_JSON)
          .asInstanceOf[PtolemyObject])
      verify(repl.out).println(
        PROCESS_JSON_ACCEPTED
      )
      assert(repl.mode == INPUT_MODE_IDML)
    }

    it("should print an error if an exception is thrown whilst processing") {
      when(repl.out.println(PROCESS_JSON_ACCEPTED))
        .thenThrow(new RuntimeException(PROCESS_JSON_EXCEPTION))
      repl.processJson(INPUT_JSON)
      verify(repl.out).println(
        PROCESS_JSON_ERROR_PREFIX
          + PROCESS_JSON_EXCEPTION
      )
    }
  }

  describe("processing of idml input") {

    val repl = new TestRepl()

    it("should attempt to parse and print the input if valid") {
      repl.processJson(INPUT_JSON)
      repl.processIdml(ptolemy)(INPUT_IDML)
      val mapping      = ptolemy.fromString(INPUT_IDML)
      val expectedJson = PtolemyJackson.default.pretty(mapping.run(repl.doc.get))

      verify(repl.out).println(expectedJson)
    }

    it("should print an error if an exception is thrown whilst processing") {
      val ptolemy      = new Ptolemy(new PtolemyConf)
      val mapping      = ptolemy.fromString(INPUT_IDML)
      val expectedJson = PtolemyJackson.default.pretty(mapping.run(repl.doc.get))

      when(repl.out.println(expectedJson))
        .thenThrow(new RuntimeException(PROCESS_IDML_EXCEPTION))
      repl.processJson(INPUT_JSON)
      repl.processIdml(ptolemy)(INPUT_IDML)
      verify(repl.out).println(
        PROCESS_IDML_ERROR_PREFIX
          + PROCESS_IDML_EXCEPTION
      )
    }
  }

  describe("processing of schema input") {

    it("should construct a mapping string and attempt to parse then print the input if valid") {
      val ptolemy = new Ptolemy(new PtolemyConf)
      val mapping =
        ptolemy.fromString(PROCESS_SCHEMA_EXPECTED_MAPPING + INPUT_SCHEMA)

      val repl = new TestRepl()
      repl.processJson(INPUT_JSON)
      val expectedJson = PtolemyJackson.default.pretty(mapping.run(repl.doc.get))

      repl.processSchema(ptolemy)(INPUT_SCHEMA)
      verify(repl.out).println(PROCESS_JSON_ACCEPTED)
      verify(repl.out).println(expectedJson)
    }

    it("should print an error if an exception is thrown whilst processing") {
      val ptolemy = new Ptolemy(new PtolemyConf)
      val mapping =
        ptolemy.fromString(PROCESS_SCHEMA_EXPECTED_MAPPING + INPUT_SCHEMA)

      val repl = new TestRepl()
      repl.processJson(INPUT_JSON)
      val output = PtolemyJackson.default.pretty(mapping.run(repl.doc.get))

      when(repl.out.println(output))
        .thenThrow(new RuntimeException(PROCESS_SCHEMA_EXCEPTION))
      repl.processSchema(ptolemy)(INPUT_SCHEMA)
      verify(repl.out)
        .println(PROCESS_SCHEMA_ERROR_PREFIX + PROCESS_SCHEMA_EXCEPTION)
    }
  }

  describe("loading of a file") {

    val repl = new TestRepl()

    it("should return a string delimited with new lines containing the file's contents") {
      var ret = repl.loadFile("repl/src/test/resources/testfile.json")
      if (ret == "") {
        ret = repl.loadFile("src/test/resources/testfile.json")
      }
      assert(ret == LOAD_FILE_EXPECTED_STRING)
    }

    it("should print an error if an exception is thrown whilst processing") {
      repl.loadFile("")
      verify(repl.out).println(LOAD_FILE_EXCEPTION)
    }
  }
}
