Building
========

This project uses sbt to define it's structure and build processes.

You can build all of the modules with `sbt package`

## Building everything with Java 17 bytecode where possible

`sbt package` alone only builds Scala 2.12.20 (Java 8 bytecode) for
everything, since that's the default `scalaVersion` for the whole build.
To get Java 17 bytecode (major version 61) for every module that can
produce it, run two commands:

```
sbt package
sbt "++2.13.16" "lang/package" "datanodes/package" "jackson/package" "circe/package" \
  "core/package" "test/package" "geo/package" "jsoup/package" "hashing/package" \
  "utils/package" "repl/package" "idmld/package" "idmldoc/package" "idmltest/package" \
  "idmltutor/package" "tool/package"
```

The first gives you the `_2.12` jars, including `idmldoc-plugin` and
`idmltest-plugin` (Java 8 - unavoidable, see below). The second gives you
`_2.13` jars for the other 16 modules, with real Java 17 bytecode.

This can't be collapsed into a single command. `idmldoc-plugin` and
`idmltest-plugin` depend on regular modules (`idmldoc`, `idmltest`,
`jackson`, `geo`, `hashing`, `jsoup`) via `.dependsOn(...)`, and sbt only
links those in-memory (no publish step needed) when both sides resolve to
the same Scala version in that session. Changing the project-wide default
`scalaVersion` to 2.13 was tried and breaks exactly this: the plugins would
still need Scala 2.12 (sbt 1.x always compiles plugins with 2.12, regardless
of sbt's own version - see `adr/0001-upgrade-to-java-17.md`), but their
dependencies would now default to 2.13, so sbt can't link them and falls
back to looking for a published `_2.12` artifact that doesn't exist. The
two separate commands above are the actual way to build everything, not a
workaround for something that could otherwise be one command.

Running tests
=============

Run the whole suite with:

```
sbt test
```

This builds and tests every module against Scala 2.12.20, which is the default
`scalaVersion` for the whole build.

## Testing the Scala 2.13 build

Most modules also cross-build against Scala 2.13.16, and get real Java 17
bytecode when compiled with it (2.12 is capped at Java 8 bytecode regardless
of patch version - see `adr/0001-upgrade-to-java-17.md`). To run the 2.13
tests, switch the Scala version explicitly:

```
sbt "++2.13.16" test
```

`idmldoc-plugin` and `idmltest-plugin` are excluded from this - they're sbt
plugins, and sbt 1.x only ever loads plugins compiled with Scala 2.12, so
they don't cross-build to 2.13. Running `sbt "++2.13.16" test` from the root
project will fail trying to resolve 2.13 dependency artifacts for them; scope
the command to the other projects instead, e.g.:

```
sbt "++2.13.16" "tool/test" "idmldoc/test" "jackson/test" "test/test"
```

## Testing a single module

Scope to the project name to avoid rebuilding everything, e.g.:

```
sbt "core/test"
sbt "lang/testOnly io.idml.lang.SomeSpecificTest"
```
