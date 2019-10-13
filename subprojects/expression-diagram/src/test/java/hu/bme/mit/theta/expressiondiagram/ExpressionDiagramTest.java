package hu.bme.mit.theta.expressiondiagram;

import hu.bme.mit.theta.core.decl.ConstDecl;
import hu.bme.mit.theta.core.model.Valuation;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.booltype.BoolType;

import java.net.StandardSocketOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import hu.bme.mit.theta.core.type.booltype.FalseExpr;
import hu.bme.mit.theta.core.type.inttype.IntType;
import org.junit.Test;

import java.util.Iterator;

import static hu.bme.mit.theta.core.decl.Decls.Const;
import static hu.bme.mit.theta.core.type.booltype.BoolExprs.*;
import static hu.bme.mit.theta.core.type.inttype.IntExprs.*;


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
        node.calculateSatisfyingSubstitutions();

        node.DFS(1);

        System.out.println("--------------------------------------");
        //node.getSatisfyingSubstitutions();
        ValuationIterator valuationIterator = new ValuationIterator(node,3);
        valuationIterator.getSatisfyingSubstitutions();

        // TODO ne legyen ennyi solver
        // TODO ne stringgel nézze a tartalmazást
        // TODO nextexpression-ba kerüljön bele az összes kiértékelés
        // TODO user adhasson meg behelyettesítést
    }

    @Test
    public void testBoolean_1() {
        final ConstDecl<BoolType> ca = Const("a", Bool());
        final ConstDecl<BoolType> cb = Const("b", Bool());
        final ConstDecl<BoolType> cc = Const("c", Bool());
        VariableSubstitution.decls.add(ca);
        VariableSubstitution.decls.add(cb);
        VariableSubstitution vsnull = new VariableSubstitution(null, null);
        VariableSubstitution vsb = new VariableSubstitution(vsnull,cb);
        VariableSubstitution vsa = new VariableSubstitution(vsb,ca);
        // (a v !b) ^ (b v c)
        Expr expr = And(Or(ca.getRef(), Not(cb.getRef())), Or(cb.getRef(), cc.getRef()));
        ExpressionNode node = new ExpressionNode(vsa);
        node.setExpression(expr);
        node.calculateSatisfyingSubstitutions();

        node.DFS(1);

        System.out.println("--------------------------------------");
        //node.getSatisfyingSubstitutions();
        ValuationIterator valuationIterator = new ValuationIterator(node, 2);
        valuationIterator.getSatisfyingSubstitutions();

        // TODO ne legyen ennyi solver
        // TODO ne stringgel nézze a tartalmazást
        // TODO nextexpression-ba kerüljön bele az összes kiértékelés
        // TODO user adhasson meg behelyettesítést
    }

    @Test
    public void testBoolean_2() {
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

        // (a v c) ^ (!a v b)
        Expr expr = And(Or(ca.getRef(), cc.getRef()), Or(Not(ca.getRef()), cb.getRef()));
        ExpressionNode node = new ExpressionNode(vsa);
        node.setExpression(expr);
        node.calculateSatisfyingSubstitutions();

        System.out.println("--------------------------------------");
        node.DFS(1);

        System.out.println("--------------------------------------");
        ValuationIterator valuationIterator = new ValuationIterator(node, 3);
        valuationIterator.getSatisfyingSubstitutions();
    }

    @Test
    public void testBoolean_3() {
        final ConstDecl<BoolType> ca = Const("a", Bool());
        final ConstDecl<BoolType> cb = Const("b", Bool());
        final ConstDecl<BoolType> cc = Const("c", Bool());
        final ConstDecl<BoolType> cd = Const("d", Bool());
        final ConstDecl<BoolType> ce = Const("e", Bool());
        VariableSubstitution.decls.add(ca);
        VariableSubstitution.decls.add(cb);
        VariableSubstitution.decls.add(cc);
        VariableSubstitution.decls.add(cd);
        VariableSubstitution.decls.add(ce);
        VariableSubstitution vsnull = new VariableSubstitution(null, null);
        VariableSubstitution vse = new VariableSubstitution(vsnull,ce);
        VariableSubstitution vsd = new VariableSubstitution(vse,cd);
        VariableSubstitution vsc = new VariableSubstitution(vsd,cc);
        VariableSubstitution vsb = new VariableSubstitution(vsc,cb);
        VariableSubstitution vsa = new VariableSubstitution(vsb,ca);

        // (a v b v c v !d v !e)
        Expr expr = Or(Or(Or(ca.getRef(), cb.getRef()), cc.getRef()) , Or(Not(cd.getRef()), Not(ce.getRef())));
        ExpressionNode node = new ExpressionNode(vsa);
        node.setExpression(expr);
        node.calculateSatisfyingSubstitutions();

        System.out.println("--------------------------------------");
        node.DFS(1);

        System.out.println("--------------------------------------");
        ValuationIterator valuationIterator = new ValuationIterator(node, 5);
        valuationIterator.getSatisfyingSubstitutions();
    }

    @Test
    public void testBoolean_4() {
        final ConstDecl<BoolType> ca = Const("a", Bool());
        final ConstDecl<BoolType> cb = Const("b", Bool());
        final ConstDecl<BoolType> cc = Const("c", Bool());
        final ConstDecl<BoolType> cd = Const("d", Bool());
        VariableSubstitution.decls.add(ca);
        VariableSubstitution.decls.add(cb);
        VariableSubstitution.decls.add(cc);
        VariableSubstitution.decls.add(cd);
        VariableSubstitution vsnull = new VariableSubstitution(null, null);
        VariableSubstitution vsd = new VariableSubstitution(vsnull,cd);
        VariableSubstitution vsc = new VariableSubstitution(vsd,cc);
        VariableSubstitution vsb = new VariableSubstitution(vsc,cb);
        VariableSubstitution vsa = new VariableSubstitution(vsb,ca);

        // (a v b) ^(c v !d)
        Expr expr = And( Or(ca.getRef(), cb.getRef()), Or(cc.getRef(), Not(cd.getRef())) );
        ExpressionNode node = new ExpressionNode(vsa);
        node.setExpression(expr);
        node.calculateSatisfyingSubstitutions();

        System.out.println("--------------------------------------");
        node.DFS(1);

        System.out.println("--------------------------------------");
        ValuationIterator valuationIterator = new ValuationIterator(node, 2);
        valuationIterator.getSatisfyingSubstitutions();
    }

    @Test
    public void testBoolean_5() {
        final ConstDecl<BoolType> ca = Const("a", Bool());
        final ConstDecl<BoolType> cb = Const("b", Bool());
        final ConstDecl<BoolType> cc = Const("c", Bool());
        final ConstDecl<IntType> cd = Const("d", Int());
        VariableSubstitution.decls.add(ca);
        VariableSubstitution.decls.add(cb);
        VariableSubstitution.decls.add(cc);
        VariableSubstitution vsnull = new VariableSubstitution(null, null);
        VariableSubstitution vsc = new VariableSubstitution(vsnull,cc);
        VariableSubstitution vsb = new VariableSubstitution(vsc,cb);
        VariableSubstitution vsa = new VariableSubstitution(vsb,ca);

        // (a v b) ^ (c v d!=10)
        Expr expr = And( Or(ca.getRef(), cb.getRef()), Or(cc.getRef(), Geq(cd.getRef(), Int(10))) );
        ExpressionNode node = new ExpressionNode(vsa);
        node.setExpression(expr);
        node.calculateSatisfyingSubstitutions();

        System.out.println("--------------------------------------");
        node.DFS(1);

        System.out.println("--------------------------------------");
        ValuationIterator valuationIterator = new ValuationIterator(node, 3);
        valuationIterator.getSatisfyingSubstitutions();
    }

    @Test
    public void testBoolean_6() {
        final ConstDecl<BoolType> ca = Const("a", Bool());
        final ConstDecl<IntType> cd = Const("d", Int());
        VariableSubstitution.decls.add(ca);
        VariableSubstitution vsnull = new VariableSubstitution(null, null);
        VariableSubstitution vsa = new VariableSubstitution(vsnull,ca);

        // (a v d>=10)
        Expr expr = Or(ca.getRef(), Geq(cd.getRef(), Int(10)));
        ExpressionNode node = new ExpressionNode(vsa);
        node.setExpression(expr);
        node.calculateSatisfyingSubstitutions();

        System.out.println("--------------------------------------");
        node.DFS(1);

        System.out.println("--------------------------------------");
        ValuationIterator valuationIterator = new ValuationIterator(node, 1);
        valuationIterator.getSatisfyingSubstitutions();
    }

    @Test
    public void testBoolean_7() {
        final List<ConstDecl<BoolType>> actLits = new ArrayList<>();

        final ConstDecl<BoolType> ca = Const("a", Bool());
        final ConstDecl<BoolType> cb = Const("b", Bool());
        final ConstDecl<IntType> cd = Const("d", Int());
        actLits.add(ca);
        actLits.add(cb);

        /*
        VariableSubstitution.decls.add(ca);
        VariableSubstitution.decls.add(cb);
        VariableSubstitution vsnull = new VariableSubstitution(null, null);
        VariableSubstitution vsb = new VariableSubstitution(vsnull,cb);
        VariableSubstitution vsa = new VariableSubstitution(vsb,ca);
        */

        VariableSubstitution vs = ExpressionNode.createDecls(actLits);

        // (!a v d<=10) ^ (b v d>10)
        Expr expr = And( Or(Not(ca.getRef()), Leq(cd.getRef(), Int(0))), Or(cb.getRef(), Gt(cd.getRef(), Int(0))) );
        ExpressionNode node = new ExpressionNode(vs);
        node.setExpression(expr);
        node.calculateSatisfyingSubstitutions();

        System.out.println("--------------------------------------");
        node.DFS(1);

        System.out.println("--------------------------------------");
        ValuationIterator valuationIterator = new ValuationIterator(node, 2);
        valuationIterator.getSatisfyingSubstitutions();
    }

    @Test
    public void testBoolean_true() {
        final List<ConstDecl<BoolType>> actLits = new ArrayList<>();
        final ConstDecl<BoolType> ca = Const("a", Bool());
        actLits.add(ca);

        VariableSubstitution vs = ExpressionNode.createDecls(actLits);

        // (!a v a)
        Expr expr = Or(Not(ca.getRef()), ca.getRef());
        //Expr expr = False();
        ExpressionNode node = new ExpressionNode(vs);
        node.setExpression(expr);
        node.calculateSatisfyingSubstitutions();

        System.out.println("--------------------------------------");
        node.DFS(1);

        System.out.println("--------------------------------------");
        ValuationIterator valuationIterator = new ValuationIterator(node, 0);
        valuationIterator.getSatisfyingSubstitutions();
    }

    @Test
    public void testBoolean_counter() {
        final List<ConstDecl<BoolType>> actLits = new ArrayList<>();
        final ConstDecl<IntType> ca = Const("a", Int());

        VariableSubstitution vs = ExpressionNode.createDecls(actLits);

        // (a>=5 ^ a<=5)
        Expr expr = And(Geq(ca.getRef(), Int(5)), Leq(ca.getRef(), Int(5)));
        //Expr expr = False();
        ExpressionNode node = new ExpressionNode(vs);
        node.setExpression(expr);
        node.calculateSatisfyingSubstitutions();

        System.out.println("--------------------------------------");
        node.DFS(1);

        System.out.println("--------------------------------------");
        ValuationIterator valuationIterator = new ValuationIterator(node, 1);
        valuationIterator.getSatisfyingSubstitutions();
    }

}
