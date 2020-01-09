import _root_.io.idml.doc.IdmlDocSbt
import _root_.io.idml.doc.IdmlDocSbt.{idmlTargetDirectory, Idml, makeIdml}
import microsites.ConfigYml

enablePlugins(IdmlDocSbt)
enablePlugins(MicrositesPlugin)

micrositeName := "IDML"

micrositeDescription := "Ingestion Data Mapping Language"

//micrositeBaseUrl := "/idml"

//micrositeDocumentationUrl := "/idml/docs"

micrositeAuthor := "fairhair.ai"

micrositeHomepage := "https://idml.io/"

micrositeOrganizationHomepage := "https://fairhair.ai"

micrositeHighlightTheme := "solarized-light"

micrositeGithubOwner := "idml"
micrositeGithubRepo := "idml"

micrositePalette := Map(
        "brand-primary"     -> "#C65736",
        "brand-secondary"   -> "#162341",
        "brand-tertiary"    -> "#1c2c52",
        "gray-dark"         -> "#453E46",
        "gray"              -> "#837F84",
        "gray-light"        -> "#E3E2E3",
        "gray-lighter"      -> "#F4F3F4",
        "white-color"       -> "#FFFFFF")

micrositeGitterChannel := false

micrositeShareOnSocial := false

micrositeDocumentationUrl := "/user-guide/getting-started.html"

idmlTargetDirectory := resourceManaged.value / "main" / "jekyll"

micrositeConfigYaml := ConfigYml(yamlCustomProperties = Map("highlighter" -> "highlightjs"))

import scala.sys.process._
lazy val index = taskKey[Unit]("generate index")
lazy val touchup = taskKey[Unit]("touch up generated html")
index := {
  "./docs/generate-index.sh" !
}
touchup := {
  "./docs/touchups.sh" !
}
makeMicrosite := Def.sequential(index, (makeIdml in Idml), touchup).value

