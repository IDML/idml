package io.idml.utils

import io.idml.datanodes.{PBool, PDouble, PInt, PString}
import io.idml.ast.{Assignment, Block, ExecNavLiteral, Pipeline, PtolemyFunction, Reassignment}
import io.idml.functions.{ArrayFunction, PtolemyFunction0, PtolemyValueFunction, SetSizeFunction}
import io.idml.PtolemyMapping
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.{JsonSerializer, ObjectMapper, SerializerProvider}
import org.json4s.jackson.Json4sScalaModule

/**
  * Provides functions for turning parsed IDML documents into JSON
  */
object JsonAstGenerator {
  val OM = new ObjectMapper()
  OM.registerModule(PtolemyAstSerializers.ptolemyAstModule)
  OM.registerModule(Json4sScalaModule)

  /**
    * Turn a parsed PtolemyMapping into a JSON string
    * @param p
    * @return
    */
  def generateJson(p: PtolemyMapping): String = {
    OM.writeValueAsString(p)
  }

  /**
    * Implicit extension to PtolemyMapping to provide .toJson
    * Usage:
    *  import io.idml.utils.JsonAstGenerator._
    *  ...
    *  ptolemy.fromString("foo = bar").toJson
    * @param m mapping to wrap
    */
  implicit class SerializablePtolemyMapping(m: PtolemyMapping) {
    def toJson: String = generateJson(m)
  }
}

/**
  * Implements serializes that are capable of turning a parsed IDML document into a JSON representation
  */
object PtolemyAstSerializers {
  val ptolemyAstModule: SimpleModule = new SimpleModule("PtolemyAst")

  class PtolemyMappingSerializer extends JsonSerializer[PtolemyMapping] {
    override def serialize(m: PtolemyMapping, jgen: JsonGenerator, sp: SerializerProvider): Unit = {
      m.nodes.blocks.filter(_._1 == "main").foreach { kv =>
        val (name, block) = kv
        jgen.writeObject(block)
      }
    }
    override def handledType(): Class[PtolemyMapping] = classOf[PtolemyMapping]
  }
  ptolemyAstModule.addSerializer(new PtolemyAstSerializers.PtolemyMappingSerializer)

  class PtolemyBlockSerializer extends JsonSerializer[Block] {
    override def serialize(b: Block, jgen: JsonGenerator, sp: SerializerProvider): Unit = {
      jgen.writeStartArray()
      b.rules.foreach { r =>
        jgen.writeStartObject()
        r match {
          case re: Reassignment =>
            jgen.writeObjectField("operation", "reassignment")
            jgen.writeObjectField("destination", re.dest.mkString("."))
            jgen.writeObject(re.exps)
          case as: Assignment =>
            jgen.writeObjectField("operation", "assignment")
            jgen.writeObjectField("destination", as.dest.mkString("."))
            jgen.writeObject(as.exps)
        }
        jgen.writeEndObject()
      }
      jgen.writeEndArray()
    }
    override def handledType(): Class[Block] = classOf[Block]
  }
  ptolemyAstModule.addSerializer(new PtolemyAstSerializers.PtolemyBlockSerializer)

  class PtolemyPipelineSerializer extends JsonSerializer[Pipeline] {
    private val TYPEFUNCTIONS =
      Set("int", "string", "geo", "bool", "date", "url")

    private def findFunction(p: Pipeline): Option[String] = {
      p.exps
        .find(_.isInstanceOf[PtolemyFunction])
        .map(_.asInstanceOf[PtolemyFunction].name)
    }

    override def serialize(p: Pipeline, jgen: JsonGenerator, sp: SerializerProvider): Unit = {
      p.exps.foreach {
        case vf: PtolemyValueFunction if TYPEFUNCTIONS.contains(vf.name) =>
          jgen.writeObjectField("type", vf.name)
        case vf: PtolemyFunction0 if TYPEFUNCTIONS.contains(vf.name) =>
          jgen.writeObjectField("type", vf.name)
        case vf: PtolemyValueFunction =>
          ptolemyValueFunction(jgen, sp, vf)
        case ssf: SetSizeFunction => // the size function doesn't work like anything else, so it's got a special case
          jgen.writeObjectField(ssf.name, ssf.arg)
        case af: ArrayFunction => // the array function takes another function so it needs to be a special case too
          arrayFunction(jgen, af)
        case enl: ExecNavLiteral =>
          execNavLiteral(jgen, enl)
        case _ =>
          throw new Exception("Unsupported pipeline type for this serializer")
      }
    }

    private def ptolemyValueFunction(jgen: JsonGenerator, sp: SerializerProvider, vf: PtolemyValueFunction): Unit = {
      vf.args.length match {
        case l: Any if l == 0 =>
          jgen.writeObjectField(vf.name, true)
        case l: Any if l == 1 =>
          jgen.writeFieldName(vf.name)
          serialize(vf.args.head, jgen, sp)
        case l: Any if l > 1 =>
          jgen.writeArrayFieldStart(vf.name)
          vf.args.foreach(serialize(_, jgen, sp))
          jgen.writeEndArray()
      }
    }

    private def arrayFunction(jgen: JsonGenerator, af: ArrayFunction): Unit = {
      af.expr match {
        case p: Pipeline =>
          jgen.writeObjectField("type", "array(%s)".format(findFunction(p).getOrElse("unknown")))
      }
    }

    private def execNavLiteral(jgen: JsonGenerator, enl: ExecNavLiteral): Unit = {
      enl.literal.value match {
        case i: PInt =>
          jgen.writeObject(i.value)
        case s: PString =>
          jgen.writeObject(s.value)
        case b: PBool =>
          jgen.writeObject(b.value)
        case d: PDouble =>
          jgen.writeObject(d.value)
        case _ =>
          throw new scala.Exception("Unsupported literal used inside JSON AST")
      }
    }

    override def handledType(): Class[Pipeline] = classOf[Pipeline]
  }

  ptolemyAstModule.addSerializer(new PtolemyAstSerializers.PtolemyPipelineSerializer)
}
