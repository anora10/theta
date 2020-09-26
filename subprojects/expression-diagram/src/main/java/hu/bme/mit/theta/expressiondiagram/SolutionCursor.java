package hu.bme.mit.theta.expressiondiagram;

import hu.bme.mit.theta.core.decl.Decl;
import hu.bme.mit.theta.core.model.ImmutableValuation;
import hu.bme.mit.theta.core.model.Valuation;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.LitExpr;
import hu.bme.mit.theta.core.type.booltype.FalseExpr;
import hu.bme.mit.theta.core.type.booltype.TrueExpr;
import hu.bme.mit.theta.solver.Solver;
import hu.bme.mit.theta.solver.z3.Z3SolverFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;

import static hu.bme.mit.theta.core.type.abstracttype.AbstractExprs.Eq;
import static hu.bme.mit.theta.core.type.abstracttype.AbstractExprs.Neq;

public class SolutionCursor {
    private HashMap<VariableSubstitution, NodeCursor> nodeCursors = new HashMap<>();
    private ExpressionNode node;
    private LitExpr lastLiteral = null;
    private Decl lastDecl = null;
    private Solver solver = Z3SolverFactory.getInstance().createSolver();

    /**
     * Constructor setting root node
     */
    public SolutionCursor (ExpressionNode n) {
        node = n;
        initiateSolver(n.expression);
    }

    /**
     * Initiate solver with expression and push
     *
     * @param e expression
     */
    private void initiateSolver(Expr e) {
        solver.reset();
        solver.add(e);
        solver.push();
    }

    /**
     * Find first SAT route from given node
     *
     * @param n node
     * @param vs VariableSubstitution of node
     * @return false, if no satisfying assignments can be found
     */
    private boolean findFirstPath(ExpressionNode n, VariableSubstitution vs) {
        solver.push();
        if (lastLiteral != null && lastLiteral != DefaultLitExpr.getInstance()) solver.add(Eq(lastDecl.getRef(), lastLiteral));
        if (vs == null || vs.next == null || n.expression.equals(TrueExpr.getInstance())) {
            if (vs != null && vs.next != null)
                nodeCursors.put(vs, n.makeCursor(solver));
            //return n.isSatisfiable();    //only for user input (either as root node or after substitution)
            clearCursors(vs);
            return true;
        }
        nodeCursors.put(vs, n.makeCursor(solver));
        boolean found;
        do {
            if (!nodeCursors.get(vs).moveNext()) {
                solver.pop();
                return false;
            }
            lastDecl = vs.getDecl();
            lastLiteral = nodeCursors.get(vs).getLiteral();
            found = findFirstPath(nodeCursors.get(vs).getNode(), vs.next);
        } while(!found);
        return true;
    }

    /**
     * Find (not first!) SAT route from given node
     *
     * @param vs VariableSubstitution of node
     * @return false, if no more satisfying assignments can be found
     */
    private boolean findNextPath(VariableSubstitution vs) {
        if (vs == null || vs.next == null || (nodeCursors.containsKey(vs) && nodeCursors.get(vs).node.expression.equals(TrueExpr.getInstance())) ) {
            solver.pop();
            return false;
        }
        if (findNextPath(vs.next))
            return true;
        boolean found;
        do {
            if (nodeCursors.containsKey(vs)) {
                LitExpr literal = nodeCursors.get(vs).getLiteral();
                if (literal != DefaultLitExpr.getInstance()) {
                    solver.add(Neq(vs.getDecl().getRef(), literal));
                }
            }
            if(!nodeCursors.containsKey(vs) || !nodeCursors.get(vs).moveNext()) {
                solver.pop();
                return false;
            }
            lastDecl = vs.getDecl();
            lastLiteral = nodeCursors.get(vs).getLiteral();
            found = findFirstPath(nodeCursors.get(vs).getNode(), vs.next);
        } while(!found);
        return true;

    }

    /**
     * Find next satisfying assignment
     *
     * @return false, if no more satisfying assignments can be found
     */
    public boolean moveNext() {
        if (node.variableSubstitution.next == null) {
            // input expression contains no literals
            boolean isSat = solver.check().isSat();
            solver.add(FalseExpr.getInstance());
            return isSat;
        }

        if (! nodeCursors.containsKey(node.variableSubstitution))
            return findFirstPath(node, node.variableSubstitution);
        return findNextPath(node.variableSubstitution);

    }

    /**
     * Clear outdated cursor values when backtracking
     *
     * @param vs VariableSubstitution, after which the values most be cleared
     */
    private void clearCursors(VariableSubstitution vs) {
        vs = vs.next;
        while (vs != null) {
            nodeCursors.remove(vs);
            vs = vs.next;
        }
    }

    /**
     * Get literal values for lastly calculated satisfying assignments
     * It must be called after moveNext(), when it gave true!
     *
     * @return map containing variable-literal pairs
     */
    public HashMap<Decl, LitExpr> getSolutionMap () {
        HashMap<Decl, LitExpr> solutionMap = new LinkedHashMap<>();
        for (VariableSubstitution vs: nodeCursors.keySet()) {
            if (nodeCursors.get(vs).getLiteral() != null) {
                solutionMap.put(vs.decl, nodeCursors.get(vs).getLiteral());
            }
        }
        return solutionMap;
    }

    /**
     * Get literal values for lastly calculated satisfying assignments
     * It must be called after moveNext(), when it gave true!
     *
     * @return map containing variable-literal pairs
     */
    public Valuation getSolutionValuation () {
        ImmutableValuation.Builder builder = ImmutableValuation.builder();
        for (VariableSubstitution vs: nodeCursors.keySet()) {
            LitExpr literal = nodeCursors.get(vs).getLiteral();
            if (literal != null && literal != DefaultLitExpr.getInstance()) {
                builder.put(vs.decl, nodeCursors.get(vs).getLiteral());
            }
        }
//        return toBuild ? builder.build() : null;
        return  builder.build();
    }

}
