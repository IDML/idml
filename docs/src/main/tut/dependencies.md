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
utils | Extra utilities, such as document classifiers
repl | the REPL
tool | the IDML tool
geo | geo functions
jsoup | HTML cleaning function and basic XML support
--- | ---
lang | the core parser, internal
datanodes | the core JSON AST, internal
geodb | tools for building the geo database, internal


### Maven

The artifacts are published to Maven so you can include it in your project with the following configuration:

```xml
<dependency>
    <groupId>io.idml</groupId>
    <artifactId>ptolemy-core_2.12</artifactId>
    <version>1.0.xxx</version>
</dependency>
```

### SBT

The dependency can also be managed with SBT:

```
libraryDependencies += "io.idml.ptolemy" %% "ptolemy-core" % "1.0.xxx"
```
