package hu.bme.mit.theta.expressiondiagram;

import com.koloboke.collect.map.ObjObjCursor;
import com.microsoft.z3.BoolExpr;
import hu.bme.mit.theta.core.decl.Decl;
import hu.bme.mit.theta.core.model.Valuation;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.LitExpr;
import hu.bme.mit.theta.core.type.Type;
import hu.bme.mit.theta.core.type.booltype.BoolLitExpr;
import hu.bme.mit.theta.core.type.booltype.BoolType;
import hu.bme.mit.theta.core.type.booltype.FalseExpr;
import hu.bme.mit.theta.core.type.booltype.TrueExpr;
import hu.bme.mit.theta.core.type.inttype.IntLitExpr;
import hu.bme.mit.theta.core.type.inttype.IntType;
import hu.bme.mit.theta.expressiondiagram.utils.SolverCallUtil;
import hu.bme.mit.theta.solver.Solver;
import hu.bme.mit.theta.solver.SolverStatus;
import hu.bme.mit.theta.solver.z3.Z3SolverFactory;

import java.math.BigInteger;
import java.util.Map;

public class NodeCursor {
    ExpressionNode node;
    private ExpressionNode newNode; // new Node
    private LitExpr<? extends Type> literal; // new Literal
    private ObjObjCursor<LitExpr<? extends Type>, ExpressionNode> mapCursor;


    Solver solver;
    private static Map<Decl<?>, LitExpr<?>> modelMap = null;

    /**
     * Constructor for cursor that searces for satisfying substitutions using a DFS method
     *
     * @param n node
     */
    NodeCursor(ExpressionNode n, Solver solver) {
        node = n;
        mapCursor = node.nextExpression.cursor();
        this.solver = solver;
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
    LitExpr getLiteral() {
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
        if (node.nextExpression.size() >= node.variableSubstitution.getMaxsize())
            node.isFinal = true;
        if (node.isFinal) {
            // node revisited, no more solutions
            return false;
        }
        if (! ExpressionNode.containsDecl(node.expression, node.variableSubstitution.getDecl())) {
            // decl not present in expr, default next node
            literal = DefaultLitExpr.getInstance();
            newNode = node.substitute(literal);
            node.isFinal = true;
            return true;
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
        SolutionCursor.logger.write("Solver check ", this);
        SolverCallUtil.increaseSolverCalls(solver);
        SolverStatus status = solver.check();
        if (status.isUnsat()) {
            // no more satisfying assignments
            node.isFinal = true;
            return false;
        }
        SolutionCursor.logger.write("Solver get model ", this);
        Valuation model = solver.getModel();
        /// save model as queue
        modelMap = model.toMap();
        //return saveSolverLiteral();
        return saveAllSolverLiterals();
    }

    private LitExpr<? extends Type> getDefaultLitexpr(Decl<? extends Type> decl) {
        if (decl.getType().toString().equals("Bool"))
            return FalseExpr.getInstance();
        if (decl.getType().toString().equals("Int"))
            return IntLitExpr.of(BigInteger.ZERO);
        // only bool and int types are supported
        assert (false);
        return null;
    }


    /**
     * Save literal value given by solver
     *
     * @return true if new solution literal saved
     */
    private boolean saveSolverLiteral() {
        Decl<? extends Type> decl = node.variableSubstitution.getDecl();
        literal = modelMap.get(decl);
        if (literal == null) {
            // The solver said that the value of decl does not count
            // We choose it false, but later it will be checked whether true is ok.
                literal = getDefaultLitexpr(decl);
            // literal = DefaultLitExpr.getInstance();
        }
        // not inspected assignment found, create new node accordingly
        newNode = node.substitute(literal);
        if (newNode == null || newNode.expression.equals(FalseExpr.getInstance())) {
            newNode.isFinal = true;
            return false;
        }
        if (newNode.expression.equals(TrueExpr.getInstance())) {
            newNode.isFinal = true;
            return true;
        }
        return true;
    }

    private boolean saveAllSolverLiterals() {
        if (node.variableSubstitution == null || node.variableSubstitution.getDecl() == null) return true;

        if (! ExpressionNode.containsDecl(node.expression, node.variableSubstitution.getDecl())) {
            // decl not present in expr, default next node
            literal = DefaultLitExpr.getInstance();
            newNode = node.substitute(literal);
            node.isFinal = true;
            return true;
        } else {
            boolean answer = saveSolverLiteral();
            if (answer == false) {
                newNode.isFinal = true;
                return false;
            }
            if (newNode.expression.equals(TrueExpr.getInstance())) {
                newNode.isFinal = true;
                return true;
            }
        }
        NodeCursor nodeCursor = newNode.makeCursor(solver);
        nodeCursor.saveAllSolverLiterals();
        return true;
    }
}