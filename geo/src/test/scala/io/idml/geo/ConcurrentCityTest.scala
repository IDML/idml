package io.idml.geo

import java.util.concurrent.Executors

import org.scalatest.{FlatSpec, MustMatchers}

import scala.concurrent.ExecutionContext

class ConcurrentCityTest extends FlatSpec with MustMatchers {

  implicit val pool = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(8))

  "City.get" should "be parallelizable" in {
    val cf = new CityFunction("org.sqlite.JDBC", "jdbc:sqlite::resource:cities.test.db", "", "")
    val r1 = (1 to 100).toList.map { i =>
      cf.get(i)
    }
    val r2 = (1 to 100).toList.par.map { i =>
      cf.get(i)
    }
    r1 must equal(r2)
  }

}
