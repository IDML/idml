package io.idml.datanodes.serder

import io.idml.PtolemyValue
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.{BeanDescription, DeserializationConfig, JavaType, Module, SerializationConfig}
import com.fasterxml.jackson.databind.Module.SetupContext
import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.ser.Serializers

/** A Jackson module that adds support for DataNodes */
class PtolemyJacksonModule extends Module {

  def getModuleName: String = "PtolemyModule"

  def version(): Version = Version.unknownVersion()

  def setupModule(ctxt: SetupContext) {
    ctxt.addSerializers(PValueSerializerResolver)
    ctxt.addDeserializers(PValueDeserializerResolver)
  }
}

/** The singleton instance of the Jackson module that adds support for DataNodes */
object PtolemyJacksonModule extends PtolemyJacksonModule

/** An object that activates the de-serialization of PValues */
private object PValueDeserializerResolver extends Deserializers.Base {
  private val pvalue = classOf[PtolemyValue]
  override def findBeanDeserializer(javaType: JavaType, config: DeserializationConfig, beanDesc: BeanDescription): PValueDeserializer = {
    if (!pvalue.isAssignableFrom(javaType.getRawClass)) {
      // scalastyle:off null
      null
      // scalastyle:on null
    } else {
      new PValueDeserializer(config.getTypeFactory, javaType.getRawClass)
    }
  }
}

/** An object that activates the serialization of PValues */
private object PValueSerializerResolver extends Serializers.Base {
  private val pvalue = classOf[PtolemyValue]
  override def findSerializer(config: SerializationConfig, theType: JavaType, beanDesc: BeanDescription): PValueSerializer = {
    if (!pvalue.isAssignableFrom(theType.getRawClass)) {
      // scalastyle:off null
      null
      // scalastyle:on null
    } else {
      new PValueSerializer
    }
  }
}
