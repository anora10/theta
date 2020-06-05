package hu.bme.mit.theta.expressiondiagram.simplifier;

import hu.bme.mit.theta.core.model.Valuation;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.Type;
import hu.bme.mit.theta.core.utils.ExprSimplifier;

public interface BasicSimplifier {
    static Expr<? extends Type> simplify(final Expr<? extends Type> expr, final Valuation valuation) {
        return ExprSimplifier.simplify(expr, valuation);
    }
}
