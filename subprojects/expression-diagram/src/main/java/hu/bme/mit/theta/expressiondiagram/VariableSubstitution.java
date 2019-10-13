package hu.bme.mit.theta.expressiondiagram;

import hu.bme.mit.theta.core.decl.Decl;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.Type;

import java.util.ArrayList;


public class VariableSubstitution {
    UniqueTable uniqueTable = new UniqueTable();
    Decl<? extends Type> decl = null;
    static ArrayList<Decl<? extends Type>> decls = new ArrayList<>();
    VariableSubstitution next = null;

    /**
     * Constructor, VS stores which decl is the next to be substituted
     *
     * @param d is to be substituted
     * @param nextvs is the following VS
     */
    VariableSubstitution (VariableSubstitution nextvs, Decl d) {
        next = nextvs;
        decl = d;
    }

    /**
     * Checks whether expression exists on the given level, if not, adds it to the unique table
     *
     * @param expression
     * @return the found or newly added node
     */
    ExpressionNode checkIn(Expr expression) {
        // is the after substitution resulting expression in the map?
        ExpressionNode node = uniqueTable.get(expression);
        // if map already contains the node
        if (node != null) {
            return node;
        }
        // else node must be added
        node = new ExpressionNode(next);
        node.setExpression(expression);
        uniqueTable.addExpressionNode(expression, node);
        return node;
    }

    /**
     * Return the decl which is to substitute
     *
     * @return decl
     */
    Decl<? extends Type> getDecl() {
        return decl;
    }

    /**
     * Return next decl
     *
     * @param d actual decl
     * @return next decl
     */
    Decl getNextDecl(Decl d) {
        //for (int i = 0; i < decls.size()-1; i++) {
        //    if (decls.get(i) == d) return decls.get(i+1);
        //}
        if (next == null) return null;
        return next.getDecl();
    }
}
