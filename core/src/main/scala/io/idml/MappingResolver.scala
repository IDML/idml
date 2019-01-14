package io.idml

import java.io.FileInputStream

import org.apache.commons.io.IOUtils

/** Interface for something that resolves mappings from a file system or something */
abstract class MappingResolver {
  def resolveAndLoad(path: String): String
}

/** Resolve a file */
class FileResolver extends MappingResolver {
  override def resolveAndLoad(path: String): String = {
    try {
      IOUtils.toString(new FileInputStream(path))
    } catch {
      case ex: Exception =>
        throw new AssertionError(s"Couldn't load file $path", ex)
    }
  }
}

/** Resolve a resource from the classpath */
class ResourceResolver extends MappingResolver {
  // scalastyle:off null
  override def resolveAndLoad(path: String): String = {
    IOUtils.toString(
      getClass
        .getResourceAsStream(path)
        .ensuring(_ != null, s"Couldn't load resource $path"))
  }
  // scalastyle:on null
}
