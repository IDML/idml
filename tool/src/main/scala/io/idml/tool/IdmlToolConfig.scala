package io.idml.tool

import java.io.File

case class IdmlToolConfig(files: List[File] = List.empty,
                          pretty: Boolean = false,
                          unmapped: Boolean = false,
                          strict: Boolean = false,
                          traceFile: Option[File] = None)
