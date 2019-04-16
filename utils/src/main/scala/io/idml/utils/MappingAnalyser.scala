package io.idml.utils

import io.idml.PtolemyMapping
import io.idml.utils.folders.Folders._

object MappingAnalyser {

  def extractInputs(m: PtolemyMapping): Unit = {
    val blocks = m.nodes.blocks
    m.nodes.main.rules.flatMap { r =>
      r.fold(
        _.exps.exps,
        _.exps.exps,
        _.exps.exps
      )
    }.map { e =>
      e.fold(
        _ => None,
        _ => None,
        _ => None,
        _ => None,
        _ => None,
        _ => None,
        _ => None,
        _ => None,
        _ => None,
        _ => None,
        _ => None,
        _ => None,
        _ => None,
        _ => None
      )
    }
  }

}
