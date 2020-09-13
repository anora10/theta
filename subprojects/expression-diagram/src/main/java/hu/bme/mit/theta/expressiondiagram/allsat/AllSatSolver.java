package hu.bme.mit.theta.expressiondiagram.allsat;

import hu.bme.mit.theta.core.decl.ConstDecl;
import hu.bme.mit.theta.core.decl.Decl;
import hu.bme.mit.theta.core.model.Valuation;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.LitExpr;

import java.beans.Expression;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public interface AllSatSolver extends Iterator<Valuation> {
    Expression expression = null;

    // init functions
    void setK (HashMap<Decl, Integer> ks);
    void setK(int k);
    void init (Expr expr, List<? extends Decl> decls);
    void init (Expr expr);

    // use functions
    HashMap<Decl, LitExpr> nextMap();


    default HashSet<Valuation> getAllSolutions() {
        HashSet<Valuation> solutions = new HashSet<>();
        Valuation newSolution = next();
        while (hasNext() && newSolution != null) {
            solutions.add(newSolution);
            newSolution = next();
        }
        return solutions;
    }

    @Override
    boolean hasNext();

    @Override
    Valuation next();
}