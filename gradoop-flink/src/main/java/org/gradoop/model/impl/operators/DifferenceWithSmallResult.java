/*
 * This file is part of Gradoop.
 *
 * Gradoop is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gradoop is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gradoop.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gradoop.model.impl.operators;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.graph.Edge;
import org.apache.flink.graph.Graph;
import org.apache.flink.graph.Vertex;
import org.apache.flink.util.Collector;
import org.gradoop.model.impl.EPFlinkEdgeData;
import org.gradoop.model.impl.EPFlinkGraphData;
import org.gradoop.model.impl.EPFlinkVertexData;
import org.gradoop.model.impl.EPGraphCollection;
import org.gradoop.model.impl.Subgraph;

import java.util.Iterator;
import java.util.List;

import static org.gradoop.model.impl.EPGraph.*;

public class DifferenceWithSmallResult extends
  AbstractBinaryCollectionToCollectionOperator {
  @Override
  protected EPGraphCollection executeInternal(EPGraphCollection firstCollection,
    EPGraphCollection secondGraphCollection) throws Exception {

    DataSet<Tuple2<Subgraph<Long, EPFlinkGraphData>, Long>> thisGraphs =
      firstSubgraphs.map(
        new AbstractBinaryCollectionToCollectionOperator
          .Tuple2LongMapper<Subgraph<Long, EPFlinkGraphData>>(
          1L));

    DataSet<Tuple2<Subgraph<Long, EPFlinkGraphData>, Long>> otherGraphs =
      secondSubgraphs
        .map(new Tuple2LongMapper<Subgraph<Long, EPFlinkGraphData>>(2L));

    final DataSet<Subgraph<Long, EPFlinkGraphData>> newSubgraphs =
      thisGraphs.union(otherGraphs)
        .groupBy(new SubgraphTupleKeySelector<Long>()).reduceGroup(
        new GroupReduceFunction<Tuple2<Subgraph<Long, EPFlinkGraphData>,
          Long>, Subgraph<Long, EPFlinkGraphData>>() {

          @Override
          public void reduce(
            Iterable<Tuple2<Subgraph<Long, EPFlinkGraphData>, Long>> iterable,
            Collector<Subgraph<Long, EPFlinkGraphData>> collector) throws
            Exception {
            Iterator<Tuple2<Subgraph<Long, EPFlinkGraphData>, Long>> it =
              iterable.iterator();
            Tuple2<Subgraph<Long, EPFlinkGraphData>, Long> subgraph = null;
            Boolean inOtherCollection = false;
            while (it.hasNext()) {
              subgraph = it.next();
              if (subgraph.f1.equals(2l)) {
                inOtherCollection = true;
              }
            }
            if (!inOtherCollection) {
              collector.collect(subgraph.f0);
            }
          }
        });

    final List<Long> identifiers = newSubgraphs
      .map(new MapFunction<Subgraph<Long, EPFlinkGraphData>, Long>() {
        @Override
        public Long map(Subgraph<Long, EPFlinkGraphData> subgraph) throws
          Exception {
          return subgraph.getId();
        }
      }).collect();

    DataSet<Vertex<Long, EPFlinkVertexData>> vertices =
      firstGraph.getVertices();
    vertices =
      vertices.filter(new FilterFunction<Vertex<Long, EPFlinkVertexData>>() {

        @Override
        public boolean filter(Vertex<Long, EPFlinkVertexData> vertex) throws
          Exception {
          boolean vertexInGraph = false;
          for (Long id : identifiers) {
            if (vertex.getValue().getGraphs().contains(id)) {
              vertexInGraph = true;
              break;
            }
          }
          return vertexInGraph;
        }
      });

    DataSet<Edge<Long, EPFlinkEdgeData>> edges = firstGraph.getEdges();

    edges = edges.join(vertices).where(SOURCE_VERTEX_ID).equalTo(VERTEX_ID)
      .with(new EdgeJoinFunction()).join(vertices).where(TARGET_VERTEX_ID)
      .equalTo(VERTEX_ID).with(new EdgeJoinFunction());

    return new EPGraphCollection(Graph.fromDataSet(vertices, edges, env),
      newSubgraphs, env);
  }

  @Override
  public String getName() {
    return "DifferenceWithSmallResult";
  }
}