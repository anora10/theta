package hu.bme.mit.theta.expressiondiagram;

import com.koloboke.collect.map.ObjObjCursor;
import hu.bme.mit.theta.core.decl.Decl;
import hu.bme.mit.theta.core.model.Valuation;
import hu.bme.mit.theta.core.type.LitExpr;
import hu.bme.mit.theta.core.type.Type;
import hu.bme.mit.theta.core.type.booltype.FalseExpr;
import hu.bme.mit.theta.solver.SolverStatus;

class NodeCursor {
    private ExpressionNode node, newNode;
    private LitExpr litExpr;
    public Decl decl;
    private ObjObjCursor<LitExpr<? extends Type>, ExpressionNode> mapCursor;


    /**
     * Constructor for cursor that searces for satisfying substitutions using a DFS method
     *
     * @param n node
     */
    NodeCursor(ExpressionNode n) {
        node = n;
        decl = node.variableSubstitution.getDecl();
        mapCursor = node.nextExpression.cursor();
    }

    /**
     * Return lastly calculated node
     *
     * @return node
     */
    ExpressionNode getNode() {
        return newNode;
    }

    /**
     * Return lastly calculated value
     *
     * @return value
     */
    LitExpr<? extends Type> getLiteral() {
        return litExpr;
    }

    void getCachedResult() {
        newNode = mapCursor.value();
        litExpr = mapCursor.key();
        if (litExpr != null && litExpr != new DefaultLitExpr()) {}
    }

    boolean getNewResult() {
        SolverStatus status = node.solver.check();
        if (status.isUnsat()) {
            // no more satisfying assignments
            node.isFinal = true;
            return false;
        }
        Valuation model = node.solver.getModel();
        /// save model as queue
        node.modelMap = model.toMap();
        boolean result = saveSolverSolution();
        return  result;
    }

    boolean saveSolverSolution() {
        litExpr = node.modelMap.get(decl);
        if (litExpr != null) {}
        else {
            // The solver said that the decl may be both true or false.
            // We choose it false, but later it will be checked whether true is ok.
            litExpr = FalseExpr.getInstance();
            //solver.add(Neq(decl.getRef(), litExpr));
        }

        // not inspected assignment found, create new node accordingly
        newNode = node.substitute(litExpr);
        if (newNode != null && !newNode.isFinal) {
            newNode.makeCursor().saveSolverSolution();
        }
        if (newNode == null) return false;
        return true;
    }

    /**
     * Search for next satisfying substitution
     * litExpr will be the calculated value
     * if node is final, there is no next to move to
     *
     * @return false, if no more satisfying assignments can be found
     */
    boolean moveNext() { // gives false, if no more satisfying assignments can be found
        //System.out.println("        moveNExt Expression " + node.expression.toString());
        if (mapCursor != null && mapCursor.moveNext()) { // it is not a recursive call!
            // cached result found
            getCachedResult();
            //System.out.println("    From cache: " + litExpr.toString() + " instead of " + variableSubstitution.getDecl().toString() + " into " + expression.toString());
            return true;
        }
        mapCursor = null;
        if (node.isFinal || decl == null) return false;
        boolean result = getNewResult();
        return result;
    }
}