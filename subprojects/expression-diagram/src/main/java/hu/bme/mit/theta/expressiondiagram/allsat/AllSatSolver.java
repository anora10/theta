package hu.bme.mit.theta.expressiondiagram.allsat;

import hu.bme.mit.theta.core.decl.ConstDecl;
import hu.bme.mit.theta.core.decl.Decl;
import hu.bme.mit.theta.core.model.Valuation;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.LitExpr;

import java.beans.Expression;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public interface AllSatSolver {
    Expression expression = null;

    // init functions
    void setK (HashMap<Decl, Integer> ks);
    void setK(int k);
    void init (Expr expr, List<? extends Decl> decls);
    void init (Expr expr);

    // use functions
    HashMap<Decl, LitExpr> getNextSolution();
    Valuation getNextSolutionValuation();

    boolean hasNextSolution();

    default HashSet<HashMap<Decl, LitExpr>> getAllSolutions() {
        HashSet<HashMap<Decl, LitExpr>> solutions = new HashSet<>();
        HashMap<Decl, LitExpr> newSolution = getNextSolution();
        while (hasNextSolution()) {
            solutions.add(newSolution);
            newSolution = getNextSolution();
        }
        return solutions;
    }
}