package io.idml.datanodes

import java.net.URL

import io.idml.{IdmlString, IdmlValue, MissingField}

/** Represents a valid URL */
class IUrl(url: URL) extends IdmlString with CompositeValue {

  /** The URL represented as a string */
  val value: String = url.toString

  /** Override get(..) to provide custom field accessors */
  override def get(name: String): IdmlValue =
    name match {
      case "host"     => IdmlValue(url.getHost)
      case "protocol" => IdmlValue(url.getProtocol)
      case "query"    => IdmlValue(url.getQuery)
      case "path"     => IdmlValue(url.getPath)
      case _          => MissingField
    }

  override def toString: String = value
}
