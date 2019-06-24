package hu.bme.mit.theta.expressiondiagram;

import hu.bme.mit.theta.core.type.Expr;

import java.util.HashMap;

public class UniqueTable {
    HashMap<Expr, ExpressionNode> map = new HashMap<>();

    // register new expression-node pair
    void addExpressionNode (Expr expression, ExpressionNode node) {
        map.put(expression, node);
    }

    // return node of given expression
    ExpressionNode get(Expr expression) {
        // if asked key expression is not in map return null
        if (map.containsKey(expression)) return map.get(expression);
        return null;
    }

}
