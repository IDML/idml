package io.idml.functions

import java.lang.reflect.Method

import io.idml.ast.{Pipeline, PtolemyFunction}
import io.idml.{PtolemyContext, PtolemyValue}

/** Allows the invocation of PtolemyValue methods. Acts as a gateway for calls like 'x.int()' in the mapping language
  *
  * TODO experiment with different reflection styles. Current benchmarks show reflectasm and Java reflection perform
  * on par in this case, but further investigation is warranted
  *
  * @param method exact method to be run
  * @param args arguments to be given to it
  * @param isNAry boolean indicating if the function takes N arguments
  *               this is required so that arguments can be coerced into a Sequence before being passed,
  *               it also disables the argument length check.
  */
case class PtolemyValueFunction(method: Method, args: List[Pipeline], isNAry: Boolean = false) extends PtolemyFunction {

  val name = method.getName

  if (!isNAry) {
    require(method.getParameterTypes.length == args.length,
            s"method takes ${method.getGenericParameterTypes.length} parameters but given ${args.length}")
  }
  require(classOf[PtolemyValue].isAssignableFrom(method.getReturnType), s"method cannot return type ${method.getReturnType}")
  require(
    method.getParameterTypes.forall(p => p.isAssignableFrom(classOf[PtolemyValue]) || p == classOf[Seq[PtolemyValue]]),
    s"method parameters of incorrect types: ${method.getParameterTypes}"
  )

  /** Evaluate the current runtime values of the arguments and pass them to the method handle */
  def invoke(ctx: PtolemyContext) {
    val r           = args.map(_.eval(ctx))
    val runtimeArgs = if (isNAry) Seq(r) else r

    ctx.enterFunc(this)

    ctx.cursor = method.invoke(ctx.cursor, runtimeArgs: _*).asInstanceOf[PtolemyValue]

    ctx.exitFunc(this)
  }
}
