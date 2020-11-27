package hu.bme.mit.theta.expressiondiagram.utils;

import hu.bme.mit.theta.common.visualization.Graph;

public class DiagramToGraphUtil {

    /**
     * Get the number of nodes
     *
     * @param graph examined graph
     * @return number of nodes in graph
     */
    public static Integer getNodeNumber(Graph graph) {
        return graph.getNodes().size();
    }

    /**
     * Get the number of edges
     *
     * @param graph examined graph
     * @return number of edges in graph
     */
    public static Integer getEdgeNumber(Graph graph) {
        return graph.getEdges().size();
    }
}
