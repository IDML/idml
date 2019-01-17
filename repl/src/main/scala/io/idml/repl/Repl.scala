// scalastyle:off regex
package io.idmlrepl

import java.io.PrintWriter
import java.util.Properties

import io.idml.datanodes.PObject
import io.idml._
import io.idml.hashing.HashingFunctionResolver
import io.idml.jsoup.{JsoupFunctionResolver, PtolemyJsoup}

import scala.sys.ShutdownHookThread
import scala.tools.jline.TerminalFactory
import scala.tools.jline.console.ConsoleReader
import scala.collection.JavaConverters._

/**
  * An IDML repl
  *
  * @author Stuart Dallas <stuart.dallas@datasift.com>
  */
class Repl {

  val ptolemy = new Ptolemy(
    new PtolemyConf,
    new StaticFunctionResolverService(
      (StaticFunctionResolverService.defaults.asScala
        ++ List(new JsoupFunctionResolver(), new HashingFunctionResolver())).asJava)
  )

  val build        = BuildInfo
  val IDML_VERSION = build.version
  val BUILD_DATE   = build.builtAtString

  val reader = new ConsoleReader()
  reader.getTerminal.setEchoEnabled(false)
  val out                  = new PrintWriter(reader.getOutput)
  var mode: String         = "json"
  var doc: Option[PObject] = None

  def run(args: Array[String]) {
    addShutdownHook()

    out.println("""
        |idml %s (%s)
        |
        |Type ".help" for instructions on how to use this tool. Press ctrl+c to exit.
      """.stripMargin.format(IDML_VERSION, BUILD_DATE))
    out.println()

    try {
      while (mode != "quit") {
        var input = getInput(mode)
        if (input(0) == '.') {
          // It's a command. Tokenise the rest of the line.
          val tokens = input.substring(1).split("""\s+""")
          tokens(0).toLowerCase match {
            case "help"                  => displayHelp()
            case ("quit" | "exit" | "q") => mode = "quit"
            case "json"                  => mode = "json"
            case "idml"                  => mode = "idml"
            case "schema"                => mode = "schema"
            case "load" =>
              input = loadFile(input.substring(".load".length).trim)
              if (input.length > 0) {
                processInput(input)
              }
            case _ =>
              out.println("Error: Unknown command [" + tokens(0) + "]")
              ""
          }
        } else {
          processInput(input)
        }
        out.println()
      }
    } catch {
      // scalastyle:off null
      case e: Throwable if e.getMessage != null =>
        println("Fatal: " + e.getMessage)
      // scalastyle:on null
      case _: Throwable => None
    }
  }

  def addShutdownHook(): ShutdownHookThread = {
    sys.addShutdownHook({
      try {
        TerminalFactory.get().restore()
      } catch {
        // scalastyle:off null
        case e: Throwable if e.getMessage != null => println(e.getMessage)
        // scalastyle:on null
        case _: Throwable => None
      }
    })
  }

  def displayHelp() {
    out.println("""
                  |This REPL consists of several modes: json, idml, and schema. The current mode
                  |is identified by the prompt. In all modes you can simply type your input,
                  |ending with an empty line.
                  |
                  |In JSON mode you are entering the JSON document with which you want to work.
                  |Your JSON must consist of a single JSON document.
                  |
                  |In IDML mode you are entering the IDML mapping you want to run against the last
                  |JSON document entered.
                  |
                  |In Schema mode you are entering a schema you want to run against the last JSON
                  |document entered.
                  |
                  |At any prompt (but not at a continuation prompt) you can enter the following
                  |commands:
                  |
                  |  .help             Display this help message.
                  |  .quit/.q/.exit    Exit the REPL.
                  |  .json             Switch to JSON input mode.
                  |  .idml             Switch to IDML input mode.
                  |  .load <filename>  Use the specified file as input.
                  |  .schema           Switch to Schema input mode.
                """.stripMargin)
  }

  def processInput(input: String) {
    mode match {
      case "json"   => processJson(input)
      case "idml"   => processIdml(input)
      case "schema" => processSchema(input)
      case _        => out.println("Error: Unknown mode [" + mode + "]")
    }
  }

  def processJson(input: String) {
    // Store and parse the JSON.
    try {
      doc = Some(PtolemyJson.parse(input).asInstanceOf[PObject])
      out.println("JSON accepted")
      mode = "idml"
    } catch {
      case e: Throwable =>
        out.println("Error: " + e.getMessage)
    }
  }

  def processIdml(input: String) {
    try {
      val mapping = ptolemy.fromString(input)
      val output  = mapping.run(doc.get)
      out.println(PtolemyJson.pretty(output))
    } catch {
      case e: Throwable =>
        out.println("Error: " + e.getMessage)
    }
  }

  def processSchema(input: String) {
    try {
      var mapping: String = ""
      doc.get.fields.keySet.foreach {
        case key: String =>
          mapping += key + " = " + key + "\n"
      }
      val schema = ptolemy.fromString(mapping + input)
      val output = schema.run(doc.get)
      out.println(PtolemyJson.pretty(output))
    } catch {
      case e: Throwable =>
        out.println("Error: " + e.getMessage)
    }
  }

  def getInput(prompt: String): String = {
    var input = ""

    reader.setPrompt(prompt + "> ")
    while (input.trim.isEmpty) {
      input = reader.readLine().trim
    }

    if (input.charAt(0) != '.') {
      var line = ""
      reader.setPrompt(("." * prompt.length) + "> ")
      do {
        line = reader.readLine.trim
        input += "\n" + line
      } while (line.nonEmpty)
    }
    input
  }

  def loadFile(filename: String): String = {
    try {
      val source = scala.io.Source.fromFile(filename)
      val retval = source.getLines() mkString "\n"
      source.close()
      retval
    } catch {
      case e: Throwable =>
        out.println("Error: " + e.getMessage)
        ""
    }
  }
}
// scalastyle:on regex
