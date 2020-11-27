package hu.bme.mit.theta.expressiondiagram.utils;

import hu.bme.mit.theta.common.visualization.EdgeAttributes;
import hu.bme.mit.theta.common.visualization.Graph;
import hu.bme.mit.theta.common.visualization.NodeAttributes;
import hu.bme.mit.theta.core.type.LitExpr;
import hu.bme.mit.theta.core.type.Type;
import hu.bme.mit.theta.expressiondiagram.ExpressionNode;

import java.util.*;

public class DiagramToGraphUtil {

    static Boolean visualize = false;

    static int cnt = 0;
    static int maxId = 0;

    public static Boolean getVisualize() {return visualize;}

    public static void setVisualize(Boolean visualize) {
        DiagramToGraphUtil.visualize = visualize;
    }

    /**
     * Transform underlying substitution diagram to graph
     *
     * @param node root node of substitution diagram
     * @return graph after transformation
     */
    public static Graph toGraph(ExpressionNode node) {
        Graph graph = new Graph(Integer.toString(cnt),node.getExpression().toString());
        Queue<ExpressionNode> queue = new LinkedList<>();
        List<String> visited = new ArrayList<>();
        Map<String,Integer> idMap = new HashMap<>();
        queue.add(node);
        node.setNodeId(maxId++);
        visited.add(node.getNodeLabel());
        graph.addNode(node.getNodeId(), NodeAttributes.builder().label(node.getNodeLabel()).build());

        //BFS loop
        while (!queue.isEmpty()) {
            ExpressionNode currentNode = queue.remove();
            for (LitExpr<? extends Type> edgeLabel : currentNode.getNextExpression().keySet()) {
                ExpressionNode tempNode = currentNode.getNextExpression().get(edgeLabel);
                if (! visited.contains(tempNode.getNodeLabel())) {
                    tempNode.setNodeId(maxId++);
                    graph.addNode(tempNode.getNodeId(), NodeAttributes.builder().label(tempNode.getNodeLabel()).build());
                    visited.add(tempNode.getNodeLabel());
                    queue.add(tempNode);
                }
                graph.addEdge(currentNode.getNodeId(),tempNode.getNodeId(), EdgeAttributes.builder().label(edgeLabel.toString()).build());
                //graph.setChild(currentNode.getNodeId(),tempNode.getNodeId());
            }
        }
        cnt++;
        return graph;
    }


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
