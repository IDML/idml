---
layout: docsplus
title: Dependencies
section: api
---

## Adding to a project

The currently available modules are:

Module | Description
--- | ---
core | The core of the language, this is the main interpreter
jackson | The Jackson JSON bindings, the default JSON parser
circe | Circe JSON bindings, the default JSON parser for scala projects
jsoup | HTML cleaning function and XML bindings
utils | Extra utilities, such as document classifiers

Modules that extend IDML to add extra functions are:

Module | Description
--- | ---
jsoup | adds XHML/HTML functions like `stripTags` and `parseXml`/`parseHtml`
jackson/circe | add `parseJson` and other associated functions for working with JSON at runtime
hashing | Adds all mainstream hash functions
geo | Geo functions like ISO code lookups

Other modules you probably don't need to depend on directly are:

Module | Description
--- | ---
bench | microbenchmarks for finding performance regressions
repl | the REPL
tool | the IDML tool
idmltutor | the interactive tutor
test | the test framework
idmldoc | the documentation framework
idmltest-plugin | the sbt plugin for idmltest
idmldoc-plugin | the sbt plugin for idmldoc
lang | the core parser
datanodes | the core JSON AST
execnodes | the evaluation AST


### Maven

The artifacts are published to Maven so you can include it in your project with the following configuration:

```xml
<dependency>
    <groupId>io.idml</groupId>
    <artifactId>idml-core_2.12</artifactId>
    <version>2.0.xxx</version>
</dependency>
```

### SBT

The dependency can also be managed with SBT:

```
libraryDependencies += "io.idml" %% "idml-core" % "2.0.xxx"
```
