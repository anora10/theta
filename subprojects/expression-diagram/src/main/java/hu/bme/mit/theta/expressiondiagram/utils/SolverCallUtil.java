package hu.bme.mit.theta.expressiondiagram.utils;

import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.booltype.BoolType;
import hu.bme.mit.theta.solver.Solver;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SolverCallUtil {
    private static Integer solverCalls = 0;
    private static Integer exprSymbols = 0;

    public static void resetSolverCalls() {
        solverCalls = 0;
    }

    public static void increaseSolverCalls(Solver solver) {
        if (DiagramToGraphUtil.getMetrics() == true) {
            Collection<Expr<BoolType>> assertions = solver.getAssertions();
            for (Expr<BoolType> assertion : assertions) {
                exprSymbols += countSymbols(assertion);
            }
            solverCalls++;
        }
    }

    public static Integer getSolverCalls() {
        return solverCalls;
    }

    private static Integer countSymbols(Expr expr) {
        int symbols = 1;
        List<? extends Expr> ops = expr.getOps();
        for (Expr e : ops) {
            symbols += countSymbols(e);
        }
        return symbols;
    }

    public static double getAvgExprSymbols() {
        return (double) exprSymbols/solverCalls;
    }
}
