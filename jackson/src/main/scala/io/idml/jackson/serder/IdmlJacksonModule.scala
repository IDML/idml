package io.idml.jackson.serder

import io.idml.IdmlValue
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.{
  BeanDescription,
  DeserializationConfig,
  JavaType,
  Module,
  SerializationConfig
}
import com.fasterxml.jackson.databind.Module.SetupContext
import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.ser.Serializers

/** A Jackson module that adds support for DataNodes */
class IdmlJacksonModule extends Module {

  def getModuleName: String = "IdmlModule"

  def version(): Version = Version.unknownVersion()

  def setupModule(ctxt: SetupContext) {
    ctxt.addSerializers(PValueSerializerResolver)
    ctxt.addDeserializers(PValueDeserializerResolver)
  }
}

/** The singleton instance of the Jackson module that adds support for DataNodes */
object IdmlJacksonModule extends IdmlJacksonModule

/** An object that activates the de-serialization of PValues */
private object PValueDeserializerResolver extends Deserializers.Base {
  private val pvalue = classOf[IdmlValue]
  override def findBeanDeserializer(
      javaType: JavaType,
      config: DeserializationConfig,
      beanDesc: BeanDescription): PValueDeserializer = {
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
  private val pvalue = classOf[IdmlValue]
  override def findSerializer(
      config: SerializationConfig,
      theType: JavaType,
      beanDesc: BeanDescription): PValueSerializer = {
    if (!pvalue.isAssignableFrom(theType.getRawClass)) {
      // scalastyle:off null
      null
      // scalastyle:on null
    } else {
      new PValueSerializer
    }
  }
}
