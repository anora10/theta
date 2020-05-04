package hu.bme.mit.theta.expressiondiagram;

import hu.bme.mit.theta.core.decl.Decl;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.Type;

import java.util.ArrayList;


public class VariableSubstitution {
    private UniqueTable uniqueTable = new UniqueTable();
    Decl<? extends Type> decl;
    static ArrayList<Decl<? extends Type>> decls = new ArrayList<>();
    VariableSubstitution next;
    private final int maxsize;

    /**
     * Constructor, VS stores which decl is the next to be substituted
     *
     * @param d is to be substituted
     * @param nextvs is the following VS
     */
    VariableSubstitution (VariableSubstitution nextvs, Decl<? extends Type> d) {
        next = nextvs;
        decl = d;
        maxsize = 50;
    }

    int getMaxsize() {
        return maxsize;
    }

    /**
     * Checks whether expression exists on the given level, if not, adds it to the unique table
     *
     * @param expression expression to look up
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
        node = new ExpressionNode(this, expression);
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

    public static ArrayList<Decl<? extends Type>> getDecls() {
        return decls;
    }

}
