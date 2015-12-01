/*
 * This file is part of gradoop.
 *
 * gradoop is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * gradoop is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with gradoop. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * This file is part of gradoop.
 *
 * gradoop is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * gradoop is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with gradoop.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gradoop.model.impl;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.graph.Edge;
import org.apache.flink.graph.Graph;
import org.apache.flink.graph.Vertex;
import org.gradoop.model.api.EPGMEdge;
import org.gradoop.model.api.EPGMGraphHead;
import org.gradoop.model.api.EPGMVertex;
import org.gradoop.model.api.operators.GraphOperators;
import org.gradoop.model.impl.functions.bool.False;
import org.gradoop.model.impl.id.GradoopId;
import org.gradoop.util.GradoopFlinkConfig;

import java.util.Collection;

/**
 * Base class for graph representations.
 *
 * @param <G> graph data type
 * @param <V> vertex data type
 * @param <E> edge data type
 * @see LogicalGraph
 * @see GraphCollection
 */
public abstract class GraphBase<
  G extends EPGMGraphHead,
  V extends EPGMVertex,
  E extends EPGMEdge>
  implements GraphOperators<V, E> {

  /**
   * Graph data associated with the logical graphs in that collection.
   */
  protected final DataSet<G> graphHeads;

  /**
   * Gradoop Flink configuration.
   */
  private final GradoopFlinkConfig<G, V, E> config;

  /**
   * DataSet containing vertices associated with that graph.
   */
  private final DataSet<V> vertices;
  /**
   * DataSet containing edges associated with that graph.
   */
  private final DataSet<E> edges;

  /**
   * Creates a new graph instance.
   *
   * @param graphHeads  graph head data set
   * @param vertices    vertex data set
   * @param edges       edge data set
   * @param config      Gradoop Flink configuration
   */
  protected GraphBase(DataSet<G> graphHeads, DataSet<V> vertices,
    DataSet<E> edges, GradoopFlinkConfig<G, V, E> config) {
    this.graphHeads = graphHeads;
    this.vertices = vertices;
    this.edges = edges;
    this.config = config;
  }

  /**
   * Creates a graph head dataset from a given collection.
   * Encapsulates the workaround for dataset creation from an empty collection.
   *
   * @param graphHeads graph head collection
   * @param config configuration
   * @param <G> graph head type
   * @param <V> vertex type
   * @param <E> edge type
   * @return edge dataset
   */
  protected static
  <G extends EPGMGraphHead, V extends EPGMVertex, E extends EPGMEdge> DataSet<G>
  createGraphHeadDataSet(
    Collection<G> graphHeads, GradoopFlinkConfig<G, V, E> config) {

    ExecutionEnvironment env = config.getExecutionEnvironment();

    DataSet<G> graphHeadSet;
    if (graphHeads.isEmpty()) {
      graphHeads.add(config.getGraphHeadFactory().createGraphHead());
      graphHeadSet = env.fromCollection(graphHeads).filter(new False<G>());
    } else {
      graphHeadSet =  env.fromCollection(graphHeads);
    }
    return graphHeadSet;
  }

  /**
   * Creates a vertex dataset from a given collection.
   * Encapsulates the workaround for dataset creation from an empty collection.
   *
   * @param vertices vertex collection
   * @param config configuration
   * @param <G> graph head type
   * @param <V> vertex type
   * @param <E> edge type
   * @return edge dataset
   */
  protected static
  <G extends EPGMGraphHead, V extends EPGMVertex, E extends EPGMEdge> DataSet<V>
  createVertexDataSet(
    Collection<V> vertices, GradoopFlinkConfig<G, V, E> config) {

    ExecutionEnvironment env = config.getExecutionEnvironment();

    DataSet<V> vertexSet;
    if (vertices.isEmpty()) {
      vertices.add(config.getVertexFactory().createVertex());
      vertexSet = env.fromCollection(vertices).filter(new False<V>());
    } else {
      vertexSet = env.fromCollection(vertices);
    }
    return vertexSet;
  }

  /**
   * Creates an edge dataset from a given collection.
   * Encapsulates the workaround for dataset creation from an empty collection.
   *
   * @param edges edge collection
   * @param config configuration
   * @param <G> graph head type
   * @param <V> vertex type
   * @param <E> edge type
   * @return edge dataset
   */
  protected static
  <G extends EPGMGraphHead, V extends EPGMVertex, E extends EPGMEdge> DataSet<E>
  createEdgeDataSet(
    Collection<E> edges, GradoopFlinkConfig<G, V, E> config) {

    ExecutionEnvironment env = config.getExecutionEnvironment();

    GradoopId dummyId = GradoopId.get();
    DataSet<E> edgeSet;
    if (edges.isEmpty()) {
      edges.add(config.getEdgeFactory().createEdge(dummyId, dummyId));
      edgeSet = env.fromCollection(edges).filter(new False<E>());
    } else {
      edgeSet = env.fromCollection(edges);
    }
    return edgeSet;
  }

  /**
   * Returns the Gradoop Flink configuration.
   *
   * @return Gradoop Flink configuration
   */
  public GradoopFlinkConfig<G, V, E> getConfig() {
    return config;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataSet<V> getVertices() {
    return vertices;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataSet<E> getEdges() {
    return edges;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataSet<E> getOutgoingEdges(final GradoopId vertexID) {
    return
      this.edges.filter(new FilterFunction<E>() {
        @Override
        public boolean filter(E edge) throws Exception {
          return edge.getSourceId().equals(vertexID);
        }
      });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataSet<E> getIncomingEdges(final GradoopId vertexID) {
    return
      this.edges.filter(new FilterFunction<E>() {
        @Override
        public boolean filter(E edge) throws Exception {
          return edge.getTargetId().equals(vertexID);
        }
      });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Graph<GradoopId, V, E> toGellyGraph() {
    return Graph.fromDataSet(getVertices().map(
      new MapFunction<V, Vertex<GradoopId, V>>() {
        @Override
        public Vertex<GradoopId, V> map(V epgmVertex) throws Exception {
          return new Vertex<>(epgmVertex.getId(), epgmVertex);
        }
      }).withForwardedFields("*->f1"),
      getEdges().map(new MapFunction<E, Edge<GradoopId, E>>() {
        @Override
        public Edge<GradoopId, E> map(E epgmEdge) throws Exception {
          return new Edge<>(epgmEdge.getSourceId(),
            epgmEdge.getTargetId(),
            epgmEdge);
        }
      }).withForwardedFields("*->f2"),
      config.getExecutionEnvironment());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getVertexCount() throws Exception {
    return vertices.count();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getEdgeCount() throws Exception {
    return edges.count();
  }

  /**
   * Collects a boolean dataset and extracts its first member.
   *
   * @param booleanDataSet boolean dataset
   * @return first member
   * @throws Exception
   */
  protected Boolean collectEquals(DataSet<Boolean> booleanDataSet) throws
    Exception {
    return booleanDataSet
      .collect()
      .get(0);
  }
}