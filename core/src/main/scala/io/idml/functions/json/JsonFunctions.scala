package io.idml.functions.json

import io.idml.IdmlJson
import io.idml.ast.{Argument, IdmlFunction, IdmlFunctionMetadata, Pipeline}
import io.idml.functions.FunctionResolver

class JsonFunctions(json: IdmlJson) extends FunctionResolver {
  val uuid   = new UUIDModuleJson(json)
  val obj    = uuid // it already inherits it
  val random = new RandomModuleJson(json)

  override def providedFunctions(): List[IdmlFunctionMetadata] =
    List(
      IdmlFunctionMetadata("serialize", List.empty, "serialize this object as JSON"),
      IdmlFunctionMetadata("parseJson", List.empty, "parse this string as JSON"),
      IdmlFunctionMetadata("uuid3", List.empty, "generate a type 3 UUID from this input"),
      IdmlFunctionMetadata("uuid5", List.empty, "generate a type 5 UUID from this input"),
      IdmlFunctionMetadata("random", List.empty, "generate a random number from this input"),
      IdmlFunctionMetadata(
        "random",
        List(("min", "minimum number"), ("max", "maximum number")),
        "generate a random number from this input, in this range"
      )
    )

  override def resolve(name: String, args: List[Argument]): Option[IdmlFunction] =
    (name, args) match {
      case ("serialize", Nil)                                  => Some(uuid.serializeFunction)
      case ("parseJson", Nil)                                  => Some(uuid.parseJsonFunction)
      case ("uuid3", Nil)                                      => Some(uuid.uuid3Function)
      case ("uuid5", Nil)                                      => Some(uuid.uuid5Function)
      case ("random", Nil)                                     => Some(random.random0Function)
      case ("random", (p1: Pipeline) :: (p2: Pipeline) :: Nil) =>
        Some(random.random2Function(p1, p2))
      case _                                                   => None
    }
}
