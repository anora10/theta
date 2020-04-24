package hu.bme.mit.theta.expressiondiagram;

import com.koloboke.collect.map.ObjObjCursor;
import hu.bme.mit.theta.core.decl.Decl;
import hu.bme.mit.theta.core.model.Valuation;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.LitExpr;
import hu.bme.mit.theta.core.type.Type;
import hu.bme.mit.theta.core.type.booltype.FalseExpr;
import hu.bme.mit.theta.core.type.booltype.TrueExpr;
import hu.bme.mit.theta.solver.Solver;
import hu.bme.mit.theta.solver.SolverStatus;
import hu.bme.mit.theta.solver.z3.Z3SolverFactory;

import java.util.Map;

class NodeCursor {
    ExpressionNode node;
    private ExpressionNode newNode; // new Node
    private LitExpr literal; // new Literal
    private ObjObjCursor<LitExpr<? extends Type>, ExpressionNode> mapCursor;

    static Solver solver = Z3SolverFactory.getInstace().createSolver();
    private static Map<Decl<?>, LitExpr<?>> modelMap = null;

    /**
     * Initiate solver with expression and push
     *
     * @param e expression
     */
    public static void initiateSolver(Expr e) {
        solver.reset();
        solver.add(e);
        solver.push();
    }

    /**
     * Constructor for cursor that searces for satisfying substitutions using a DFS method
     *
     * @param n node
     */
    NodeCursor(ExpressionNode n) {
        node = n;
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
        return literal;
    }

    /**
     * Checks if mapCursor can move forward,
     * or adds new literal to nextExpression if possible
     *
     * @return false, if no more satisfying assignments can be found FOR DECL
     */
    boolean moveNext() { // gives false, if no more satisfying assignments can be found FOR DECL
        if (mapCursor != null && mapCursor.moveNext()) {
            // there are remaining unexploited results in map
            newNode = mapCursor.value();
            literal = mapCursor.key();
            return true;
        }
        // calculate new result
        mapCursor = null;
        if (node.isFinal) {
            // node revisited, no more solutions
            return false;
        }
        // get next solution from solver,
        // it also sets newNode and literal
        boolean result = getSolverResult();
        if (!result) {
            node.isFinal = true;
        }
        return result;
    }

    /**
     * Ask solver for new SAT assignment
     *
     * @return true, if solver found SAT assigment
     */
    private boolean getSolverResult() {
        SolverStatus status = solver.check();
        if (status.isUnsat()) {
            // no more satisfying assignments
            node.isFinal = true;
            return false;
        }
        Valuation model = solver.getModel();
        /// save model as queue
        modelMap = model.toMap();
        return saveSolverLiteral();
    }

    /**
     * Save literal value given by solver
     *
     * @return true if new solution literal saved
     */
    private boolean saveSolverLiteral() {
        Decl decl = node.variableSubstitution.getDecl();
        literal = modelMap.get(decl);
        if (literal == null) {
            // The solver said that the value of decl does not count
            // We choose it false, but later it will be checked whether true is ok.
            literal = FalseExpr.getInstance();
            // literal = DefaultLitExpr.getInstance();
        }
        // not inspected assignment found, create new node accordingly
        // TODO: substituteAll for caching solution
        newNode = node.substitute(literal);
        if (newNode == null || newNode.expression.equals(FalseExpr.getInstance())) return false;
        if (newNode.expression.equals(TrueExpr.getInstance())) return true;
        return true;
    }
}