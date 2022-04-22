package io.idml.jackson.difftool

import io.idml.IdmlValue
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.{BeanDescription, JavaType, Module, SerializationConfig}
import com.fasterxml.jackson.databind.Module.SetupContext
import com.fasterxml.jackson.databind.ser.Serializers

class DiffJacksonModule extends Module {

  def getModuleName: String = "DiffModule"

  def version(): Version = Version.unknownVersion()

  def setupModule(ctxt: SetupContext) {
    ctxt.addSerializers(DiffJacksonSerializerResolver)
  }
}

/** An object that activates the de-serialization of PValues */
private object DiffJacksonSerializerResolver extends Serializers.Base {
  private val pvalue = classOf[IdmlValue]
  override def findSerializer(
      config: SerializationConfig,
      theType: JavaType,
      beanDesc: BeanDescription): DiffSerializer = {
    if (!pvalue.isAssignableFrom(theType.getRawClass)) {
      // scalastyle:off null
      null
      // scalastyle:on null
    } else {
      new DiffSerializer
    }
  }
}
