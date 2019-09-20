package hu.bme.mit.theta.expressiondiagram;

import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.booltype.TrueExpr;

import java.util.Iterator;
import java.util.Stack;
//import hu.bme.mit.theta.solver.Stack;

class ValuationIterator {
    Stack<Pair> stack = new Stack<>();
    private int maxStackSize = -1;
    ValuationIterator(ExpressionNode node, int d) {
        // d is the number of decls we are interested in
        stack.add(new Pair(node, null));
        assert (d >= 0);
        maxStackSize = d+1;
    }

    ValuationIterator(ExpressionNode node) {
        stack.add(new Pair(node, null));
        //maxStackSize = stack.peek().node.expression.getArity();
        // the number of decls to calculate +1
        maxStackSize = ExpressionNode.getDecls(node.expression).size() +1;
    }

    boolean hasNext() {
        return !stack.empty();
    }

    Stack<Pair> next() {
        if (stack.size() == 1) {
            // first valuation, no backtrack needed
            while(true) {
                Pair pair = stack.peek();
                Expr expr = (Expr) pair.node.nextExpression.keySet().toArray()[0];
                ExpressionNode newNode = pair.node.nextExpression.get(expr);
                stack.add(new Pair(newNode, expr));
                if (newNode.expression.equals(TrueExpr.getInstance()) || stack.size() == maxStackSize) return  stack;
            }
        }
        Pair oldPair = stack.pop();
        boolean newNodeReached = false;
        while (stack.size() > 0) {
            if (newNodeReached) {
                // every node gives its first possible substitution value
                while(true) {
                    Pair pair = stack.peek();
                    Expr expr = (Expr) pair.node.nextExpression.keySet().toArray()[0];
                    ExpressionNode newNode = pair.node.nextExpression.get(expr);
                    stack.add(new Pair(newNode, expr));
                    if (newNode.expression.equals(TrueExpr.getInstance()) || stack.size() == maxStackSize) return  stack;
                }
            }
            Pair pair = stack.peek();
            boolean found = false, hasnext = false;
            for (Expr expr : pair.node.nextExpression.keySet()) {
                if (expr.equals(oldPair.value)) found = true;
                else if (found) {
                    hasnext = true;
                    ExpressionNode newNode = pair.node.nextExpression.get(expr);
                    oldPair = new Pair(newNode, expr);
                    stack.add(oldPair);
                    newNodeReached = true;
                    if (newNode.expression.equals(TrueExpr.getInstance()) || stack.size() == maxStackSize) return stack;
                    break;
                }
            }
            // if node has no next, step back from node
            if (!hasnext && found) oldPair = stack.pop();
            else if (!found) {
                // expressionNode not yet reached
                Expr expr = (Expr) pair.node.nextExpression.keySet().toArray()[0];
                ExpressionNode newNode = pair.node.nextExpression.get(expr);
                stack.add(new Pair(newNode, expr));
            }
        }
        stack.clear();
        return null;
    }

    class Pair {
        ExpressionNode node = null;
        Expr value = null;
        Pair(ExpressionNode node, Expr value) {
            this.node = node;
            this.value = value;
        }
    }

    public void getSatisfyingSubstitutions () {
        while (this.hasNext()) {
            Stack<ValuationIterator.Pair> solutionStack = this.next();
            if (solutionStack == null) break;
            Iterator<Pair> it = solutionStack.iterator();
            while(it.hasNext()) {
                ValuationIterator.Pair pair = it.next();
                if (pair.value == null) System.out.print(pair.node.expression.toString() + "\t"); // first element
                else System.out.print(pair.value.toString() + " " + pair.node.expression.toString() + "\t");
            }
            System.out.print("\n");
        }
    }
}
