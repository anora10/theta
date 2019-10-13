package hu.bme.mit.theta.expressiondiagram;

import hu.bme.mit.theta.core.type.Expr;

import java.util.HashMap;

public class UniqueTable {
    HashMap<Expr, ExpressionNode> map = new HashMap<>();

    /**
     * Register new expression-node pair, levels are separated
     *
     * @param expression
     * @param node
     */
    void addExpressionNode (Expr expression, ExpressionNode node) {
        map.put(expression, node);
    }

    /**
     * Return node of given expression
     *
     * @param expression
     */
    ExpressionNode get(Expr expression) {
        // if asked key expression is not in map return null
        if (map.containsKey(expression)) return map.get(expression);
        return null;
    }

}
