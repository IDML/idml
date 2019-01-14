package io.idml

import org.scalatest.FunSuite
import org.scalatest.mock.MockitoSugar

class FileResolverTest extends FunSuite with MockitoSugar {

  test("Throws an error when the file doesn't exist") {
    intercept[AssertionError](new FileResolver().resolveAndLoad("missing_file"))
  }

  test("Can load a file") {
    assert(
      new FileResolver()
        .resolveAndLoad("build.sbt")
        .contains("idml-parent") === true)
  }
}

class ResourceResolverTest extends FunSuite with MockitoSugar {

  test("Throws an error when the resource doesn't exist") {
    intercept[AssertionError](new ResourceResolver().resolveAndLoad("missing_resource"))
  }

  test("Can load a resource") {
    assert(
      new ResourceResolver()
        .resolveAndLoad("/mock_resource.txt")
        .contains("ResourceResolver") === true)
  }
}
