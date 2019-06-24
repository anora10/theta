package hu.bme.mit.theta.expressiondiagram;

import hu.bme.mit.theta.core.decl.ConstDecl;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.booltype.BoolType;
import hu.bme.mit.theta.core.type.inttype.IntType;
import org.junit.Test;

import static hu.bme.mit.theta.core.decl.Decls.Const;
import static hu.bme.mit.theta.core.type.booltype.BoolExprs.*;
import static hu.bme.mit.theta.core.type.inttype.IntExprs.Int;


public final class ExpressionDiagramTest {


    @Test
    public void testBoolean() {
        final ConstDecl<BoolType> ca = Const("a", Bool());
        final ConstDecl<BoolType> cb = Const("b", Bool());
        final ConstDecl<BoolType> cc = Const("c", Bool());
        VariableSubstitution.decls.add(ca);
        VariableSubstitution.decls.add(cb);
        VariableSubstitution.decls.add(cc);
        VariableSubstitution vsnull = new VariableSubstitution(null, null);
        VariableSubstitution vsc = new VariableSubstitution(vsnull,cc);
        VariableSubstitution vsb = new VariableSubstitution(vsc,cb);
        VariableSubstitution vsa = new VariableSubstitution(vsb,ca);
        // (a v !b) ^ (b v c)
        Expr expr = And(Or(ca.getRef(), Not(cb.getRef())), Or(cb.getRef(), cc.getRef()));
        ExpressionNode node = new ExpressionNode(vsa);
        node.setExpression(expr);
        node.getSatisfyingSubstitutions();

        node.DFS(1);

        node.getSatisfyingSubstitutions();


        // TODO ne legyen ennyi solver
        // TODO ne stringgel nézze a tartalmazást
        // TODO nextexpression-ba kerüljön bele az összes kiértékelés
        // TODO user adhasson meg behelyettesítést
    }

    @Test
    public void testInteger() {

    }
}
