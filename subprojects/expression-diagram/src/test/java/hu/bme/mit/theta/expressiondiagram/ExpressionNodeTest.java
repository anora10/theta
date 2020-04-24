package hu.bme.mit.theta.expressiondiagram;

import hu.bme.mit.theta.core.decl.ConstDecl;
import hu.bme.mit.theta.core.decl.Decl;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.LitExpr;
import hu.bme.mit.theta.core.type.Type;
import hu.bme.mit.theta.core.type.booltype.BoolType;
import hu.bme.mit.theta.core.type.booltype.TrueExpr;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static hu.bme.mit.theta.core.decl.Decls.Const;
import static hu.bme.mit.theta.core.type.booltype.BoolExprs.*;

public class ExpressionNodeTest {

    // (a v !b) ^ (b v c)
    ExpressionNode node;

    @Before
    public void init() {
        final List<ConstDecl<BoolType>> actLits = new ArrayList<>();
        final ConstDecl<BoolType> ca = Const("a", Bool());
        final ConstDecl<BoolType> cb = Const("b", Bool());
        final ConstDecl<BoolType> cc = Const("c", Bool());

        VariableSubstitution.decls.add(ca);
        VariableSubstitution.decls.add(cb);
        VariableSubstitution.decls.add(cc);
        actLits.add(ca);
        actLits.add(cb);
        actLits.add(cc);

        VariableSubstitution vs = ExpressionNode.createDecls(actLits);

        // (a v !b) ^ (b v c)
        Expr expr = And(Or(ca.getRef(), Not(cb.getRef())), Or(cb.getRef(), cc.getRef()));
        node = new ExpressionNode(vs, expr);
    }

    @Test
    public void testSubstitute() {
        LitExpr falseExpr = False();
        LitExpr trueExpr = True();
        LitExpr defaultExpr = DefaultLitExpr.getInstance();
        ExpressionNode subA = node.substitute(trueExpr);
        System.out.println(subA.expression);
    }

    @Test
    public void testSubstituteAll() {
        LitExpr falseExpr = False();
        LitExpr trueExpr = True();
        LitExpr defaultExpr = DefaultLitExpr.getInstance();
        HashMap<Decl, LitExpr<? extends Type>> values = new HashMap<>();
        ExpressionNode subA = node.substituteAll(values);
        System.out.println(subA.expression);
    }

}
