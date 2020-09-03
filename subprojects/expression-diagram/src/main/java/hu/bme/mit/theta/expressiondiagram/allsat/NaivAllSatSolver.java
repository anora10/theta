package hu.bme.mit.theta.expressiondiagram.allsat;

import hu.bme.mit.theta.common.logging.ConsoleLogger;
import hu.bme.mit.theta.common.logging.Logger;
import hu.bme.mit.theta.core.decl.Decl;
import hu.bme.mit.theta.core.model.ImmutableValuation;
import hu.bme.mit.theta.core.model.Valuation;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.LitExpr;
import hu.bme.mit.theta.core.type.booltype.TrueExpr;
import hu.bme.mit.theta.expressiondiagram.ExpressionNode;
import hu.bme.mit.theta.expressiondiagram.VariableSubstitution;
import hu.bme.mit.theta.solver.Solver;
import hu.bme.mit.theta.solver.z3.Z3SolverFactory;
import static hu.bme.mit.theta.core.type.abstracttype.AbstractExprs.Eq;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static hu.bme.mit.theta.core.type.booltype.BoolExprs.And;
import static hu.bme.mit.theta.core.type.booltype.BoolExprs.Not;

public class NaivAllSatSolver implements AllSatSolver{
    private Solver solver = Z3SolverFactory.getInstance().createSolver();
    private List<? extends Decl> decls;
    boolean isFinal = false;
    private Logger logger;

    @Override
    public void setK(HashMap<Decl, Integer> ks) {
        Logger logger = new ConsoleLogger(Logger.Level.INFO);
        logger.write(Logger.Level.INFO, "Setting k in naiv AllSAT solver not yet supported.");
    }

    @Override
    public void setK(int k) {
        Logger logger = new ConsoleLogger(Logger.Level.INFO);
        logger.write(Logger.Level.INFO, "Setting k in naiv AllSAT solver not yet supported.");
    }

    @Override
    public void init(Expr expr, List<? extends Decl> decls) {
        this.decls = decls;
        solver.reset();
        solver.add(expr);
    }

    @Override
    public void init(Expr expr) {
        decls = new ArrayList<>(ExpressionNode.getDecls(expr));
        init(expr, decls);
    }

    @Override
    public HashMap<Decl, LitExpr> getNextSolution() {
        if (solver.check().isUnsat()) {
            isFinal = true;
            return null;
        }
        Valuation model = solver.getModel();
        Map<? extends Decl, ? extends LitExpr> map = model.toMap();
        HashMap<Decl, LitExpr> solutionMap = new HashMap<>();
        Expr expr = TrueExpr.getInstance();
        for (Decl d: map.keySet()) {
            if (decls.contains(d)) {
                solutionMap.put(d, map.get(d));
                expr = And(expr, Eq(d.getRef(), map.get(d)));
            }
        }
        solver.add(Not(expr));
        return solutionMap;
    }

    @Override
    public Valuation getNextSolutionValuation() {
        HashMap<Decl, LitExpr> solutionMap = getNextSolution();
        ImmutableValuation.Builder builder = ImmutableValuation.builder();
        for (Decl d: solutionMap.keySet()) {
            builder.put(d, solutionMap.get(d));
        }
        return builder.build();
    }

    @Override
    public boolean hasNextSolution() {
        return !isFinal;
    }
}
