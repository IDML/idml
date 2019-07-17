package io.idml

import java.util.ServiceLoader

import io.idml.ast.{Argument, IdmlFunction, Pipeline}
import io.idml.functions.FunctionResolver

import scala.util.Try
import cats._, cats.implicits._
import scala.collection.JavaConverters._

/** A factory for functions that utilizes the Java ServiceLoader pattern */
class FunctionResolverService {

  /** Function resolver */
  protected val loader = ServiceLoader.load(classOf[FunctionResolver], getClass.getClassLoader)

  /**
    * Create a new function given name and arguments
    *
    * @param name The name of the function
    * @param args The executable arguments list
    * @return The new function
    */
  def resolve(name: String, args: List[Argument]): IdmlFunction = {
    var result: Option[IdmlFunction] = None
    val resolvers                    = loader.iterator()
    if (!resolvers.hasNext) {
      throw new NoFunctionResolversLoadedException("""There are no function resolvers loaded.
          | Without function resolvers it's impossible to use any functions in mappings.
          | This is probably a misconfiguration of the classpath!""".stripMargin)
    }
    while (result.isEmpty && resolvers.hasNext) {
      val nxt = resolvers.next()
      result = nxt.resolve(name, args)
    }
    result.getOrElse(throw new UnknownFunctionException(s"Unsupported function '$name' with ${args.size} params"))
  }

  def orElse(other: FunctionResolverService): FunctionResolverService = {
    FunctionResolverService.orElse(this, other)
  }
}

object FunctionResolverService {
  def orElse(a: FunctionResolverService, b: FunctionResolverService): FunctionResolverService = new FunctionResolverService {
    override def resolve(name: String, args: List[Argument]): IdmlFunction =
      Try { a.resolve(name, args) }.toEither.leftMap { e1 =>
        b.resolve(name, args)
      }.merge
  }
}
