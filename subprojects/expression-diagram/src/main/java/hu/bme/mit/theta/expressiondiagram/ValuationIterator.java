package hu.bme.mit.theta.expressiondiagram;

import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.booltype.TrueExpr;

import java.util.Iterator;
import java.util.Stack;
//import hu.bme.mit.theta.solver.Stack;

public class ValuationIterator {
    private Stack<Pair> stack = new Stack<>();
    private int maxStackSize;

    /**
     * Constructor
     *
     * @param node containing the expression examined
     * @param d the number of decls to substitute
     */
    public ValuationIterator(ExpressionNode node, int d) {
        // d is the number of decls we are interested in
        stack.add(new Pair(node, null));
        assert (d >= 0);
        maxStackSize = d+1;
    }

    /**
     * Checks whether there is a next element to iterate on
     *
     * @return
     */
    public boolean hasNext() {
        return !stack.empty();
    }

    /**
     * Finds the next valuation and gives it back in the form of a stack
     *
     * @return
     */
    public Stack<Pair> next() {
        if (stack.size() == 1) {
            // first valuation, no backtrack needed
            while(true) {
                try {
                    Pair pair = stack.peek();
                    Expr expr = (Expr) pair.node.nextExpression.keySet().toArray()[0];
                    ExpressionNode newNode = pair.node.nextExpression.get(expr);
                    stack.add(new Pair(newNode, expr));
                    if (newNode.expression.equals(TrueExpr.getInstance()) || stack.size() == maxStackSize) return stack;
                } catch (Exception e) {
                    // this branch is executed if false expression occurs
                    stack.clear();
                    return null;
                }
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


    public class Pair {
        ExpressionNode node;
        Expr value;
        Pair(ExpressionNode node, Expr value) {
            this.node = node;
            this.value = value;
        }

        public Expr getValue() {return value;}
        public Expr getExpression() {return node.expression;}


    }

    /**
     * Iterates on all satisfying substitutions (they are already calculated, it does not search for new ones)
     *
     * @return
     */
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
