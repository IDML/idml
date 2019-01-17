package io.idml.utils

import java.lang.reflect.Method
import io.idml.PtolemyValue
import com.google.common.reflect.ClassPath
import scala.collection.JavaConverters.iterableAsScalaIterableConverter

/** Reports all the functions that are available from PtolemyValue and friends */
object FunctionReport extends App {

  val pv = classOf[PtolemyValue]

  /** Get all the module classes */
  def getModuleClasses: Iterable[Class[_]] = {
    // NOTE: ClassPath is marked as beta if you've recently updated guice this might explain problems you've had
    val cp = ClassPath.from(getClass.getClassLoader)

    cp.getTopLevelClasses("io.idml.datanodes.modules")
      .asScala
      .map(_.load())
  }

  /** Ensure every field is a PtolemyValue */
  def isEligibleMethod(method: Method): Boolean = {
    pv.isAssignableFrom(method.getReturnType) &&
    method.getParameterTypes.forall(pv.isAssignableFrom)
  }

  /** Get the argument list */
  def argList(method: Method): String = {
    val sb = new StringBuilder()
    for (i <- 0 until method.getParameterTypes.length) {
      if (sb.nonEmpty) {
        sb.append(", ")
      }
      sb.append(('a' + i % ('z' - 'a')).toChar)
    }

    // fn(a, b ..)
    if (method.isVarArgs) {
      sb.append(" ..")
    }

    sb.toString()

  }

  def printMethod(method: Method): Unit = {
    println(f"${method.getDeclaringClass.getSimpleName}\t${method.getName}(${argList(method)})") // scalastyle:ignore
  }

  println("Module\tFunction") // scalastyle:ignore
  getModuleClasses
    .flatMap(clazz => clazz.getMethods)
    .filter(isEligibleMethod)
    .foreach(printMethod)

}
