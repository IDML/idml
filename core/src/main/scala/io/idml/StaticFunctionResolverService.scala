package io.idml

import java.util

import io.idml.ast.{Argument, IdmlFunction, Pipeline}
import io.idml.functions.json.JsonFunctions
import io.idml.functions.{
  BuiltinFunctionResolver,
  FunctionResolver,
  IdmlValueFunctionResolver,
  IdmlValueNaryFunctionResolver
}

import scala.collection.JavaConverters._

object StaticFunctionResolverService {
  def defaults(json: IdmlJson): util.List[FunctionResolver] =
    List(
      new JsonFunctions(json),
      new BuiltinFunctionResolver,
      new IdmlValueFunctionResolver,
      new IdmlValueNaryFunctionResolver).asJava
}

class StaticFunctionResolverService(rs: java.util.List[FunctionResolver])
    extends FunctionResolverService {
  private val srs = rs.asScala

  /** Create a new function given name and arguments
    *
    * @param name
    *   The name of the function
    * @param args
    *   The executable arguments list
    * @return
    *   The new function
    */
  override def resolve(name: String, args: List[Argument]): IdmlFunction = {
    var result: Option[IdmlFunction] = None
    val resolvers                    = srs.toIterator
    if (!resolvers.hasNext) {
      throw new NoFunctionResolversLoadedException("""There are no function resolvers loaded.
          | Without function resolvers it's impossible to use any functions in mappings.
          | This is probably a misconfiguration of the classpath!""".stripMargin)
    }
    while (result.isEmpty && resolvers.hasNext) {
      result = resolvers.next().resolve(name, args)
    }
    result.getOrElse(
      throw new UnknownFunctionException(s"Unsupported function '$name' with ${args.size} params"))
  }
}
