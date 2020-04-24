package hu.bme.mit.theta.expressiondiagram;

import hu.bme.mit.theta.core.decl.Decl;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.LitExpr;
import hu.bme.mit.theta.core.type.booltype.FalseExpr;
import hu.bme.mit.theta.core.type.booltype.TrueExpr;

import java.util.HashMap;
import java.util.HashSet;

import static hu.bme.mit.theta.core.type.abstracttype.AbstractExprs.Neq;

public class SolutionCursor {
    HashMap<VariableSubstitution, NodeCursor> nodeCursors = new HashMap<>();
    private ExpressionNode node;

    public SolutionCursor (ExpressionNode n) {
        node = n;
    }

    private boolean findFirstPath(ExpressionNode n, VariableSubstitution vs) {
        NodeCursor.solver.push();
        if (vs == null || vs.next == null || n.expression.equals(TrueExpr.getInstance())) {
            // TODO false?
            // TODO literal-lal kezdeni valamit
            if (vs != null && vs.next != null)
                nodeCursors.put(vs, n.makeCursor());
            //return n.isSatisfiable();    //only for user input (either as root node or after substitution)
            clearCursors(vs);
            return true;
        }
        nodeCursors.put(vs, n.makeCursor());
        boolean found;
        do {
            if (!nodeCursors.get(vs).moveNext()) {
                NodeCursor.solver.pop();
                return false;
            }
            found = findFirstPath(nodeCursors.get(vs).getNode(), vs.next);
        } while(!found);
        return true;
    }

    private boolean findNextPath(VariableSubstitution vs) {
        if (vs == null || vs.next == null || (nodeCursors.containsKey(vs) && nodeCursors.get(vs).node.expression.equals(TrueExpr.getInstance())) ) {
            NodeCursor.solver.pop();
            return false;
        }
        if (findNextPath(vs.next))
            return true;
        boolean found;
        do {
            if (nodeCursors.containsKey(vs)) {
                LitExpr literal = nodeCursors.get(vs).getLiteral();
                NodeCursor.solver.add(Neq(vs.getDecl().getRef(),literal));
            }
            if(!nodeCursors.containsKey(vs) || !nodeCursors.get(vs).moveNext()) {
                NodeCursor.solver.pop();
                return false;
            }
            found = findFirstPath(nodeCursors.get(vs).getNode(), vs.next);
        } while(!found);
        return true;

    }

    public boolean moveNext() {
        if (node.variableSubstitution.next == null) {
            // input expression contains no literals
            boolean isSat = NodeCursor.solver.check().isSat();
            NodeCursor.solver.add(FalseExpr.getInstance());
            return isSat;
        }

        if (! nodeCursors.containsKey(node.variableSubstitution))
            return findFirstPath(node, node.variableSubstitution);
        return findNextPath(node.variableSubstitution);

    }

    private void clearCursors(VariableSubstitution vs) {
        vs = vs.next;
        while (vs != null) {
            nodeCursors.remove(vs);
            vs = vs.next;
        }
    }

    public HashMap<Decl, LitExpr> getSolutionMap () {
        HashMap<Decl, LitExpr> solutionMap = new HashMap<>();
        for (VariableSubstitution vs: nodeCursors.keySet()) {
            if (nodeCursors.get(vs).getLiteral() != null) {
                solutionMap.put(vs.decl, nodeCursors.get(vs).getLiteral());
            }
        }
        return solutionMap;
    }

}
