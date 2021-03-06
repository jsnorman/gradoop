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
package org.gradoop.examples.minimalimport;

import org.apache.flink.api.common.ProgramDescription;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.dataintegration.importer.impl.csv.MinimalCSVImporter;
import org.gradoop.examples.AbstractRunner;
import org.gradoop.flink.io.api.DataSource;
import org.gradoop.flink.model.impl.epgm.LogicalGraph;
import org.gradoop.flink.util.GradoopFlinkConfig;

/**
 * Example to show the usage of the minimal csv importer {@link MinimalCSVImporter}.
 */
public class MinimalCSVImportExample extends AbstractRunner implements ProgramDescription {

  /**
   * Example method to read a csv file and print the vertices of the generated graph.
   *
   * @param args program arguments
   * @throws Exception io exception if file not found
   */
  public static void main(String[] args) throws Exception {

    ExecutionEnvironment env = getExecutionEnvironment();
    GradoopFlinkConfig config = GradoopFlinkConfig.createConfig(env);

    String exampleFile = MinimalCSVImportExample.class.
      getResource("/data/csv/minimalcsv/person.csv").getPath();

    DataSource source = new MinimalCSVImporter(exampleFile, "\\|", config, true);

    LogicalGraph graph = source.getLogicalGraph();

    graph.getVertices().print();
  }

  @Override
  public String getDescription() {
    return MinimalCSVImportExample.class.getName();
  }
}
