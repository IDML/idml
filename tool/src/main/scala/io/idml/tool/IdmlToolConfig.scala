import java.io.File

case class IdmlToolConfig(files: List[File] = List.empty, traceOutput: Option[File] = None, pretty: Boolean = false, unmapped: Boolean = false, strict: Boolean = false)
