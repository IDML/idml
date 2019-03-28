package io.idml

import java.net.{URL, URLClassLoader}
import java.util.ServiceLoader

import io.idml.functions.FunctionResolver

/** A factory for functions that utilizes the Java ServiceLoader pattern, with a custom classloader */
class PluginFunctionResolverService(urls: Array[URL]) extends FunctionResolverService {

  // make a classloader with the URLs we got given
  protected val classLoader = new URLClassLoader(urls)

  /** Function resolver */
  override protected val loader = ServiceLoader.load(classOf[FunctionResolver], classLoader)

}
