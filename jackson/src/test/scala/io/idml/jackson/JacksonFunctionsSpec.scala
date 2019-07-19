package io.idml.jackson

import io.idml.{Idml, IdmlValue}
import io.idml.datanodes.{IInt, IObject, IString}
import io.idml.json.JsonFunctionSuite
import org.scalatest.{MustMatchers, WordSpec}

class JacksonFunctionsSpec extends JsonFunctionSuite("JacksonFunctions", new JacksonFunctions())
