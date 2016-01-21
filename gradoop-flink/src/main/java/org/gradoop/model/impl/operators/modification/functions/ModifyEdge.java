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
 * along with Gradoop. If not, see <http://www.gnu.org/licenses/>.
 */

package org.gradoop.model.impl.operators.modification.functions;

import org.gradoop.model.api.EPGMEdge;
import org.gradoop.model.api.EPGMEdgeFactory;
import org.gradoop.model.api.functions.ModificationFunction;
import org.gradoop.util.GConstants;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Modification map function for edges.
 *
 * @param <E> EPGM edge type
 */
public class ModifyEdge<E extends EPGMEdge> extends ModifyBase<E> {

  /**
   * Factory to init modified edge.
   */
  private final EPGMEdgeFactory<E> edgeFactory;

  /**
   * Constructor
   *
   * @param modificationFunction  edge modification function
   * @param edgeFactory           edge factory
   */
  public ModifyEdge(ModificationFunction<E> modificationFunction,
    EPGMEdgeFactory<E> edgeFactory) {
    super(modificationFunction);
    this.edgeFactory = checkNotNull(edgeFactory);
  }

  @Override
  protected E initFrom(E edge) {
    return edgeFactory.initEdge(edge.getId(),
      GConstants.DEFAULT_EDGE_LABEL,
      edge.getSourceId(),
      edge.getTargetId(),
      edge.getGraphIds());
  }
}
