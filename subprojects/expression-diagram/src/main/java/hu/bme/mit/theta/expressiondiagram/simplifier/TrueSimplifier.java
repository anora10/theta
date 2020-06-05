package hu.bme.mit.theta.expressiondiagram.simplifier;

import com.microsoft.z3.BoolExpr;
import hu.bme.mit.theta.core.model.Valuation;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.Type;
import hu.bme.mit.theta.core.type.abstracttype.NegExpr;
import hu.bme.mit.theta.core.type.booltype.BoolType;
import hu.bme.mit.theta.core.type.booltype.FalseExpr;
import hu.bme.mit.theta.core.type.booltype.NotExpr;
import hu.bme.mit.theta.core.type.booltype.TrueExpr;
import hu.bme.mit.theta.core.type.inttype.IntType;
import hu.bme.mit.theta.core.utils.ExprSimplifier;
import hu.bme.mit.theta.solver.Solver;
import hu.bme.mit.theta.solver.SolverStatus;
import hu.bme.mit.theta.solver.z3.Z3SolverFactory;

public class TrueSimplifier implements BasicSimplifier {

    private static Solver solver = Z3SolverFactory.getInstace().createSolver();

    public static Expr<? extends Type> simplify(final Expr<? extends Type> expr, final Valuation valuation) {
        Expr resultingExpression = BasicSimplifier.simplify(expr, valuation);
        Expr negatedResultingExpression;
        if (resultingExpression.getType().equals(BoolType.getInstance()))
            negatedResultingExpression = NotExpr.of(resultingExpression);
        else if (resultingExpression.getType().equals(IntType.getInstance()))
            negatedResultingExpression = NegExpr.create2(resultingExpression);
        else return resultingExpression;

        solver.reset();
        solver.add(negatedResultingExpression);
        SolverStatus status = solver.check();
        if (status.isUnsat())
            // negated expression not satisfiable
            return TrueExpr.getInstance();
        // negated expression satisfiable
        solver.reset();
        solver.add(resultingExpression);
         status = solver.check();
        if (status.isUnsat())
            // expression not satisfiable
            return FalseExpr.getInstance();
        return resultingExpression;
    }
}
