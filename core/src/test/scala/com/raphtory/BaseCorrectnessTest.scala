package com.raphtory

import com.google.common.hash.Hashing
import com.raphtory.algorithms.api.GraphAlgorithm
import com.raphtory.components.graphbuilder.GraphBuilder
import com.raphtory.components.spout.Spout
import com.raphtory.deployment.Raphtory
import com.raphtory.spouts.IdentitySpout
import com.raphtory.spouts.ResourceSpout
import com.raphtory.spouts.SequenceSpout

import java.nio.charset.StandardCharsets

abstract class BaseCorrectnessTest extends BaseRaphtoryAlgoTest[String] {

  override def setGraphBuilder(): GraphBuilder[String] = BasicGraphBuilder()

  def setSpout(): Spout[String] = new IdentitySpout

  private def correctResultsHash(resultsResource: String): String = {
    val source = scala.io.Source.fromResource(resultsResource)
    try resultsHash(source.getLines())
    finally source.close()
  }

  override def beforeAll(): Unit = setup()

  override def afterAll(): Unit = {}

  private def correctResultsHash(rows: IterableOnce[String]): String =
    resultsHash(rows)

  def correctnessTest(
      algorithm: GraphAlgorithm,
      graphResource: String,
      resultsResource: String,
      lastTimestamp: Int
  ): Boolean = {
    graph = Raphtory.streamGraph(ResourceSpout(graphResource), setGraphBuilder())

    val res = algorithmPointTest(
            algorithm,
            lastTimestamp
    ) == correctResultsHash(
            resultsResource
    )
    graph.stop()
    res
  }

  def correctnessTest(
      algorithm: GraphAlgorithm,
      graphEdges: Seq[String],
      results: Seq[String],
      lastTimestamp: Int
  ): Boolean = {
    graph = Raphtory.streamGraph(SequenceSpout(graphEdges: _*), setGraphBuilder())
    val res = algorithmPointTest(
            algorithm,
            lastTimestamp
    ) == correctResultsHash(
            results
    )
    graph.stop()
    res
  }
}
