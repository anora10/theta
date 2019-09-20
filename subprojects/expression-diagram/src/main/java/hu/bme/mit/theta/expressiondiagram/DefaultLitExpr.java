package hu.bme.mit.theta.expressiondiagram;

import hu.bme.mit.theta.core.model.Valuation;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.LitExpr;
import hu.bme.mit.theta.core.type.Type;
import hu.bme.mit.theta.core.type.booltype.BoolLitExpr;
import hu.bme.mit.theta.core.type.booltype.BoolType;

import java.util.List;

public class DefaultLitExpr implements LitExpr {

    @Override
    public int getArity() {
        return 0;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public LitExpr eval(Valuation val) {
        return null;
    }

    @Override
    public List<? extends Expr<?>> getOps() {
        return null;
    }

    @Override
    public Expr withOps(List ops) {
        return null;
    }

    @Override
    public String toString() {
        return "default";
    }
}
