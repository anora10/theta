package hu.bme.mit.theta.expressiondiagram;

import hu.bme.mit.theta.core.decl.Decl;
import hu.bme.mit.theta.core.model.BasicSubstitution;
import hu.bme.mit.theta.core.model.ImmutableValuation;
import hu.bme.mit.theta.core.model.Substitution;
import hu.bme.mit.theta.core.model.Valuation;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.LitExpr;
import hu.bme.mit.theta.core.type.Type;
import hu.bme.mit.theta.core.type.inttype.IntAddExpr;
import hu.bme.mit.theta.core.type.inttype.IntLitExpr;
import hu.bme.mit.theta.core.type.inttype.IntType;
import hu.bme.mit.theta.core.utils.ExprSimplifier;
import hu.bme.mit.theta.solver.Solver;
import hu.bme.mit.theta.solver.SolverFactory;
import hu.bme.mit.theta.solver.SolverStatus;
import hu.bme.mit.theta.solver.z3.Z3SolverFactory;

import java.math.BigInteger;

public class Example {
    private Expr<? extends Type> expr;
    private Decl<? extends Type> decl;
    private LitExpr<IntType> lit;

    public Example(Expr<? extends Type> expr) {
        this.expr = expr;

        ImmutableValuation val = ImmutableValuation.builder().put(decl, lit).build();
        expr.getType();

        lit = IntLitExpr.of(BigInteger.valueOf(5));
        SolverFactory solverFactory = Z3SolverFactory.getInstance();
        Solver solver = solverFactory.createSolver();
        solver.add(val.toExpr());
        SolverStatus status = solver.check();
        status.isSat();
        Valuation model = solver.getModel();
        LitExpr<?> value = model.toMap().get(decl);
        // TODO: cursorba kell getNode és getLiteral is

//        Substitution substitution = BasicSubstitution.builder().put(decl, value).build();
//        Expr<? extends Type> resultingExpression = substitution.apply(expr);

        ImmutableValuation valuation = ImmutableValuation.builder().put(decl, value).build();

        Expr<? extends Type> resultingExpression = ExprSimplifier.simplify(expr, valuation);
    }
}
