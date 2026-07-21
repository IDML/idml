# 1. Upgrade to Java 17

## Status

Accepted

## Context

The project needed to move from Java 8 to Java 17. That turned out to mean
two things: build/run on a JDK 17 host, and actually emit Java 17 bytecode
(classfile major version 61) instead of an unchanged Java-8-targeted build
running on a newer JVM.

Getting to real bytecode 17 hit a hard constraint, verified with `javap`
rather than assumed: **Scala 2.12's compiler backend cannot emit bytecode
newer than major version 52 (Java 8), at any patch version** - even 2.12.20
with `-release 17` set still produces major version 52. Two modules,
`idmldoc-plugin` and `idmltest-plugin`, are `sbtPlugin := true` projects, and
sbt 1.x always compiles plugins with Scala 2.12.x regardless of the sbt
release - so those two modules can never move off Scala 2.12, and therefore
never off bytecode 52, while the build stays on sbt 1.x.

## Decision

- Bumped sbt 1.5.1 → 1.11.0, Scala 2.12.13 → 2.12.20, Scala 2.13.5 → 2.13.16,
  kind-projector 0.11.3 → 0.13.3 (which required updating its `?`
  placeholder syntax to `*` in `idmltest`). Every module keeps
  cross-building both Scala versions, exactly as before.
- Made the Scala-version-conditional `scalacOptions`/`javacOptions` branch
  state its actual intent instead of a misleading no-op:
  - Scala 2.13 (everything except the two plugin modules): `-release 17` /
    `--release 17` - real bytecode 61.
  - Scala 2.12 (forced for `idmldoc-plugin`/`idmltest-plugin`, available
    everywhere else too): `-target:jvm-1.8` - bytecode 52, as it always was.

## Consequences

- Every module still publishes both `_2.12` and `_2.13` artifacts. `_2.13`
  jars are genuine Java 17 bytecode (major version 61); `_2.12` jars
  (including both plugin modules) remain Java 8 bytecode (major version
  52) - confirmed directly from the packaged jars via `javap`.
- Running the full cross-built test suite needs an explicit Scala-version
  switch and, for 2.13, scoping away from the two plugin modules (see
  `BUILD.md`).
