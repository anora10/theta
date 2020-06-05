package hu.bme.mit.theta.expressiondiagram;

import hu.bme.mit.theta.core.decl.ConstDecl;
import hu.bme.mit.theta.core.decl.Decl;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.LitExpr;
import hu.bme.mit.theta.core.type.booltype.BoolType;
import hu.bme.mit.theta.core.type.inttype.IntType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static hu.bme.mit.theta.core.decl.Decls.Const;
import static hu.bme.mit.theta.core.type.abstracttype.AbstractExprs.*;
import static hu.bme.mit.theta.core.type.booltype.BoolExprs.*;
import static hu.bme.mit.theta.core.type.inttype.IntExprs.Int;

public class SolutionCursorTest {

    private ExpressionNode node;

    private void makeSolutions(ExpressionNode node) {
        SolutionCursor solutionCursor = new SolutionCursor(node);
        while (solutionCursor.moveNext()) {
            System.out.println("---megoldas---");
            HashMap<Decl, LitExpr> solutions = solutionCursor.getSolutionMap();
            for (Decl d: solutions.keySet()) {
                System.out.println(d.toString() + " " + solutions.get(d).toString());
            }
            System.out.println("\n");
        }
        System.out.println("no more solutions");
    }


    @Test // (a v !b) ^ (b v c)
    public void test1() {
        final List<ConstDecl<?>> actLits = new ArrayList<>();
        final ConstDecl<BoolType> ca = Const("a", Bool());
        final ConstDecl<BoolType> cb = Const("b", Bool());
        final ConstDecl<BoolType> cc = Const("c", Bool());

        VariableSubstitution.decls.add(ca);
        VariableSubstitution.decls.add(cb);
        VariableSubstitution.decls.add(cc);
        actLits.add(ca);
        actLits.add(cb);
        actLits.add(cc);

        VariableSubstitution vs0 = ExpressionNode.createDecls(actLits, true);

        // (a v !b) ^ (b v c)
        Expr expr = And(Or(ca.getRef(), Not(cb.getRef())), Or(cb.getRef(), cc.getRef()));
        node = new ExpressionNode(vs0, expr);
        NodeCursor.initiateSolver(expr);

        //------------------------- end of init -------------------------

        makeSolutions(node);
    }

    @Test // (a v b v c v !d v !e)
    public void test2() {
        final List<ConstDecl<?>> actLits = new ArrayList<>();
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
        actLits.add(ca);
        actLits.add(cb);
        actLits.add(cc);
        actLits.add(cd);
        actLits.add(ce);

        VariableSubstitution vs0 = ExpressionNode.createDecls(actLits, true);

        // (a v b v c v !d v !e)
        Expr expr = Or(Or(Or(ca.getRef(), cb.getRef()), cc.getRef()) , Or(Not(cd.getRef()), Not(ce.getRef())));
        ExpressionNode node = new ExpressionNode(vs0, expr);
        NodeCursor.initiateSolver(expr);

        //------------------------- end of init -------------------------

        makeSolutions(node);

    }

    @Test // (a v b v !c)
    public void test3() {
        final List<ConstDecl<?>> actLits = new ArrayList<>();
        final ConstDecl<BoolType> ca = Const("a", Bool());
        final ConstDecl<BoolType> cb = Const("b", Bool());
        final ConstDecl<BoolType> cc = Const("c", Bool());
        VariableSubstitution.decls.add(ca);
        VariableSubstitution.decls.add(cb);
        VariableSubstitution.decls.add(cc);
        actLits.add(ca);
        actLits.add(cb);
        actLits.add(cc);

        VariableSubstitution vs0 = ExpressionNode.createDecls(actLits, true);

        // (a v b v !c)
        Expr expr = Or(Or(ca.getRef(), cb.getRef()), Not(cc.getRef()));
        ExpressionNode node = new ExpressionNode(vs0, expr);
        NodeCursor.initiateSolver(expr);

        //------------------------- end of init -------------------------

        makeSolutions(node);
    }

    @Test // (a v b)
    public void test4() {
        final List<ConstDecl<?>> actLits = new ArrayList<>();
        final ConstDecl<BoolType> ca = Const("a", Bool());
        final ConstDecl<BoolType> cb = Const("b", Bool());
        VariableSubstitution.decls.add(ca);
        VariableSubstitution.decls.add(cb);
        actLits.add(ca);
        actLits.add(cb);

        VariableSubstitution vs0 = ExpressionNode.createDecls(actLits, true);

        // (a v b)
        Expr expr = Or(ca.getRef(), cb.getRef());
        ExpressionNode node = new ExpressionNode(vs0, expr);
        NodeCursor.initiateSolver(expr);

        //------------------------- end of init -------------------------

        makeSolutions(node);
    }

    @Test // (!a v a)
    public void test5() {
        final List<ConstDecl<?>> actLits = new ArrayList<>();
        final ConstDecl<BoolType> ca = Const("a", Bool());
        actLits.add(ca);

        VariableSubstitution vs0 = ExpressionNode.createDecls(actLits, true);

        // (!a v a)
        Expr expr = Or(Not(ca.getRef()), ca.getRef());
        //Expr expr = False();
        ExpressionNode node = new ExpressionNode(vs0,expr);
        NodeCursor.initiateSolver(expr);

        //------------------------- end of init -------------------------

        makeSolutions(node);
    }

    @Test // (!a v d<=0) ^ (b v d>0)
    public void test6() {
        final List<ConstDecl<?>> actLits = new ArrayList<>();

        final ConstDecl<BoolType> ca = Const("a", Bool());
        final ConstDecl<BoolType> cb = Const("b", Bool());
        final ConstDecl<IntType> cd = Const("d", Int());
        actLits.add(ca);
        actLits.add(cb);

        VariableSubstitution vs0 = ExpressionNode.createDecls(actLits, true);

        // (!a v d<=0) ^ (b v d>0)
        Expr expr = And( Or(Not(ca.getRef()), Leq(cd.getRef(), Int(0))), Or(cb.getRef(), Gt(cd.getRef(), Int(0))) );
        ExpressionNode node = new ExpressionNode(vs0, expr);
        NodeCursor.initiateSolver(expr);

        //------------------------- end of init -------------------------

        makeSolutions(node);
    }

    @Test // (!a ^ a)  --- false ---
    public void test7() {
        final List<ConstDecl<?>> actLits = new ArrayList<>();
        final ConstDecl<BoolType> ca = Const("a", Bool());
        actLits.add(ca);

        VariableSubstitution vs0 = ExpressionNode.createDecls(actLits, true);

        // (!a v a)
        Expr expr = And(Not(ca.getRef()), ca.getRef());
        //Expr expr = False();
        ExpressionNode node = new ExpressionNode(vs0,expr);
        NodeCursor.initiateSolver(expr);

        //------------------------- end of init -------------------------

        makeSolutions(node);
    }

    @Test // FalseExpr
    public void test_false() {
        final List<ConstDecl<?>> actLits = new ArrayList<>();

        VariableSubstitution vs0 = ExpressionNode.createDecls(actLits, true);

        // False
        Expr expr = False();
        ExpressionNode node = new ExpressionNode(vs0,expr);
        NodeCursor.initiateSolver(expr);

        //------------------------- end of init -------------------------

        makeSolutions(node);
    }

    @Test // TrueExpr
    public void test_true() {
        final List<ConstDecl<?>> actLits = new ArrayList<>();

        VariableSubstitution vs0 = ExpressionNode.createDecls(actLits, true);

        // True
        Expr expr = True();
        ExpressionNode node = new ExpressionNode(vs0,expr);
        NodeCursor.initiateSolver(expr);

        //------------------------- end of init -------------------------

        makeSolutions(node);
    }

    @Test // (a ^ (0 < d <= 5))
    public void test_int_1() {
        final List<ConstDecl<?>> actLits = new ArrayList<>();

        final ConstDecl<BoolType> ca = Const("a", Bool());
        final ConstDecl<IntType> cd = Const("d", Int());
        actLits.add(ca);
        actLits.add(cd);

        VariableSubstitution vs0 = ExpressionNode.createDecls(actLits, true);

        // (a ^ (0 < d <= 5))
        Expr expr = And( And( Gt(cd.getRef(), Int(0)), Leq(cd.getRef(), Int(5))), ca.getRef() );
        ExpressionNode node = new ExpressionNode(vs0, expr);
        NodeCursor.initiateSolver(expr);

        //------------------------- end of init -------------------------

        makeSolutions(node);
    }

    @Test // (a v !b) ^ (b v (0 < d <= 2))
    public void test_int_2() {
        final List<ConstDecl<?>> actLits = new ArrayList<>();

        final ConstDecl<IntType> cd = Const("d", Int());
        final ConstDecl<BoolType> ca = Const("a", Bool());
        final ConstDecl<BoolType> cb = Const("b", Bool());
        actLits.add(cd);
        actLits.add(ca);
        actLits.add(cb);

        VariableSubstitution vs0 = ExpressionNode.createDecls(actLits, true);

        // (a v !b) ^ (b v (0 < d <= 2))
        Expr expr = And( Or (cb.getRef(), (And( Gt(cd.getRef(), Int(0)), Leq(cd.getRef(), Int(2))))),
                         Or (ca.getRef(), Not(cb.getRef())));
        ExpressionNode node = new ExpressionNode(vs0, expr);
        NodeCursor.initiateSolver(expr);

        //------------------------- end of init -------------------------

        makeSolutions(node);
    }

    @Test // ((a^b) v (0 < d <= 2))
    public void test_int_3() {
        final List<ConstDecl<?>> actLits = new ArrayList<>();

        final ConstDecl<IntType> cd = Const("d", Int());
        final ConstDecl<BoolType> ca = Const("a", Bool());
        final ConstDecl<BoolType> cb = Const("b", Bool());
        actLits.add(cd);
        actLits.add(ca);
        actLits.add(cb);

        VariableSubstitution vs0 = ExpressionNode.createDecls(actLits, true);

        // ((a^b) v (0 < d <= 2))
        Expr expr = Or (And(ca.getRef(),cb.getRef()), (And( Gt(cd.getRef(), Int(0)), Leq(cd.getRef(), Int(2)))));
        ExpressionNode node = new ExpressionNode(vs0, expr);
        NodeCursor.initiateSolver(expr);

        //------------------------- end of init -------------------------

        makeSolutions(node);
    }

    @Test // (d <= 3)
    public void test_int_4() {
        final List<ConstDecl<?>> actLits = new ArrayList<>();

        final ConstDecl<IntType> cd = Const("d", Int());
        actLits.add(cd);

        VariableSubstitution vs0 = ExpressionNode.createDecls(actLits, true);

        // (d <= 3)
        Expr expr = Leq(cd.getRef(), Int(3));
        ExpressionNode node = new ExpressionNode(vs0, expr);
        NodeCursor.initiateSolver(expr);

        //------------------------- end of init -------------------------

        makeSolutions(node);
    }

    @Test // 2020 ropi zh linprog feladata
    public void test_int_peti() {
        final List<ConstDecl<?>> actLits = new ArrayList<>();

        final ConstDecl<IntType> cx1 = Const("x1", Int());
        final ConstDecl<IntType> cx2 = Const("x2", Int());
        final ConstDecl<IntType> cy = Const("y", Int());
        actLits.add(cx1);
        actLits.add(cx2);
        actLits.add(cy);

        VariableSubstitution vs0 = ExpressionNode.createDecls(actLits, true);

        // y = 3x1 + 2x2
        Expr e1 = Eq(cy.getRef(), Add(Mul(cx1.getRef(), Int(3)), Mul(cx2.getRef(), Int(2))));
        // 2x1 + x2 <= 14
        Expr e2 = Leq(Add(Mul(cx1.getRef(), Int(2)), Mul(cx2.getRef(), Int(1))) , Int(14));
        // 0 <= x1 <= 5
        Expr e3 = (And( Geq(cx1.getRef(), Int(0)), Leq(cx1.getRef(), Int(5))));
        // 0 <= x2 <= 8
        Expr e4 = (And( Geq(cx2.getRef(), Int(0)), Leq(cx2.getRef(), Int(8))));

        Expr expr = And(e1,e2,e3,e4);

        ExpressionNode node = new ExpressionNode(vs0, expr);
        NodeCursor.initiateSolver(expr);

        //------------------------- end of init -------------------------

        makeSolutions(node);
    }
}
