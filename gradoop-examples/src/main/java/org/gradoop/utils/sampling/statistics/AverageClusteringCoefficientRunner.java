/*
 * Copyright © 2014 - 2019 Leipzig University (Database Research Group)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradoop.utils.sampling.statistics;

import org.apache.flink.api.common.ProgramDescription;
import org.apache.flink.api.java.DataSet;
import org.gradoop.examples.AbstractRunner;
import org.gradoop.flink.algorithms.gelly.clusteringcoefficient.GellyLocalClusteringCoefficientDirected;
import org.gradoop.flink.model.impl.epgm.LogicalGraph;
import org.gradoop.flink.model.impl.functions.tuple.ObjectTo1;
import org.gradoop.flink.model.impl.operators.sampling.statistics.AverageClusteringCoefficient;
import org.gradoop.flink.model.impl.operators.sampling.statistics.SamplingEvaluationConstants;
import org.gradoop.flink.model.impl.operators.statistics.writer.StatisticWriter;

/**
 * Calls the computation of the average clustering coefficient for a directed logical graph.
 * Uses {@link AverageClusteringCoefficient} which calls the Gradoop-Wrapper
 * {@link GellyLocalClusteringCoefficientDirected} of Flinks ClusteringCoefficient-algorithm.
 * Writes the average value to a csv-file named
 * {@value SamplingEvaluationConstants#FILE_CLUSTERING_COEFFICIENT_AVERAGE} in the output directory,
 * e.g.:
 * <pre>
 * BOF
 * 0.2916
 * EOF
 * </pre>
 */
public class AverageClusteringCoefficientRunner
  extends AbstractRunner implements ProgramDescription {

  /**
   * Calls the computation of the average clustering coefficient for the graph.
   *
   * <pre>
   * args[0] - path to graph
   * args[1] - format of graph (csv, json, indexed)
   * args[2] - output path
   * </pre>
   *
   * @param args command line arguments
   * @throws Exception in case of read/write failure
   */
  public static void main(String[] args) throws Exception {

    LogicalGraph graph = readLogicalGraph(args[0], args[1]);

    graph = new AverageClusteringCoefficient().execute(graph);

    DataSet<Double> average = graph.getGraphHead().map(
      gh -> gh.getPropertyValue(AverageClusteringCoefficient.PROPERTY_KEY_AVERAGE).getDouble());

    StatisticWriter.writeCSV(average.map(new ObjectTo1<>()), appendSeparator(args[2]) +
      SamplingEvaluationConstants.FILE_CLUSTERING_COEFFICIENT_AVERAGE);

    getExecutionEnvironment().execute("Sampling Statistics: Average clustering coefficient");
  }

  @Override
  public String getDescription() {
    return AverageClusteringCoefficientRunner.class.getName();
  }
}
