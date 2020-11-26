package hu.bme.mit.theta.expressiondiagram;

import hu.bme.mit.theta.common.logging.ConsoleLogger;
import hu.bme.mit.theta.common.logging.Logger;
import hu.bme.mit.theta.core.decl.ConstDecl;
import hu.bme.mit.theta.core.decl.Decl;
import hu.bme.mit.theta.core.model.Valuation;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.LitExpr;
import hu.bme.mit.theta.core.type.booltype.BoolExprs;
import hu.bme.mit.theta.core.type.booltype.BoolType;
import hu.bme.mit.theta.core.type.booltype.FalseExpr;
import hu.bme.mit.theta.core.type.booltype.TrueExpr;
import hu.bme.mit.theta.core.type.inttype.IntType;
import hu.bme.mit.theta.expressiondiagram.allsat.AllSatSolver;
import hu.bme.mit.theta.expressiondiagram.allsat.BddAllSatSolver;
import hu.bme.mit.theta.expressiondiagram.allsat.BddAllSatSolverFactory;
import hu.bme.mit.theta.expressiondiagram.allsat.NaivAllSatSolverFactory;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static hu.bme.mit.theta.core.decl.Decls.Const;
import static hu.bme.mit.theta.core.type.abstracttype.AbstractExprs.Leq;
import static hu.bme.mit.theta.core.type.booltype.BoolExprs.*;
import static hu.bme.mit.theta.core.type.booltype.BoolExprs.Bool;
import static hu.bme.mit.theta.core.type.booltype.SmartBoolExprs.And;
import static hu.bme.mit.theta.core.type.inttype.IntExprs.Int;

public class AllSatSolverTest {

    AllSatSolver allSatSolver = BddAllSatSolverFactory.getInstance().createSolver();

    private void makeSolutions() {
        while (allSatSolver.hasNext()) {
            Valuation solutions = allSatSolver.next();
            if (solutions == null) continue;
            System.out.println("---megoldas---");
            for (Decl d: solutions.toMap().keySet()) {
                System.out.println(d.toString() + " " + solutions.toMap().get(d).toString());
            }
            System.out.println("\n");
        }
        System.out.println("no more solutions");

        try {
            allSatSolver.writeGraph();
        } catch (final Throwable ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Test // (a v !b) ^ (b v c)
    public void test_1() {

        final List<ConstDecl<?>> actLits = new ArrayList<>();
        final ConstDecl<BoolType> ca = Const("a", Bool());
        final ConstDecl<BoolType> cb = Const("b", Bool());
        final ConstDecl<BoolType> cc = Const("c", Bool());

        actLits.add(cc);
        actLits.add(ca);
        actLits.add(cb);

        List<String> variableOrder = new ArrayList<>();
        variableOrder.add("c");
        variableOrder.add("b");
        variableOrder.add("a");
        BddAllSatSolver.setVariableOrder(variableOrder);

        // (a v !b) ^ (b v c)
        Expr<?> expr = BoolExprs.And(Or(ca.getRef(), Not(cb.getRef())), Or(cb.getRef(), cc.getRef()));
        allSatSolver.init(expr, actLits);
        makeSolutions();
    }

    @Test // (a v b v c v !d v !e)
    public void test_2() {

        final List<ConstDecl<?>> actLits = new ArrayList<>();
        final ConstDecl<BoolType> ca = Const("a", Bool());
        final ConstDecl<BoolType> cb = Const("b", Bool());
        final ConstDecl<BoolType> cc = Const("c", Bool());
        final ConstDecl<BoolType> cd = Const("d", Bool());
        final ConstDecl<BoolType> ce = Const("e", Bool());
        actLits.add(ce);
        actLits.add(cd);
        actLits.add(cc);
        actLits.add(cb);
        actLits.add(ca);


        // (a v b v c v !d v !e)
        Expr<?> expr = Or(Or(Or(ca.getRef(), cb.getRef()), cc.getRef()) , Or(Not(cd.getRef()), Not(ce.getRef())));
        allSatSolver.init(expr, actLits);
        makeSolutions();
    }

    @Test // (0 <= x <= 5)
    public void test_3() {

        final ConstDecl<IntType> x = Const("x", Int());
        final ConstDecl<IntType> y = Const("y", Int());
        List<Decl<?>> decls = new ArrayList<>();
        decls.add(x);
        decls.add(y);

        // (0 <= x <= 5)
        Expr expr = And(Leq(Int(0), x.getRef()), Leq(x.getRef(), Int(5)));
        allSatSolver.init(expr, decls);
        makeSolutions();
    }

    @Test // (false)
    public void test_false() {

        final ConstDecl<IntType> x = Const("x", Int());
        final ConstDecl<IntType> y = Const("y", Int());
        List<Decl<?>> decls = new ArrayList<>();
        decls.add(y);
        decls.add(x);

        // false
        Expr expr = FalseExpr.getInstance();
        allSatSolver.init(expr, decls);
        makeSolutions();
    }

    @Test // (true)
    public void test_true() {

        final ConstDecl<IntType> x = Const("x", Int());
        final ConstDecl<IntType> y = Const("y", Int());
        List<Decl<?>> decls = new ArrayList<>();
        decls.add(y);
        decls.add(x);

        // true
        Expr expr = TrueExpr.getInstance();
        allSatSolver.init(expr, decls);
        makeSolutions();
    }

    @Test // (x ^ !x)
    public void test_4() {

        final ConstDecl<BoolType> x = Const("x", Bool());
        final ConstDecl<BoolType> y = Const("y", Bool());
        List<Decl<?>> decls = new ArrayList<>();
        decls.add(x);
        decls.add(y);


        // (x ^ !x)
        Expr expr = And(x.getRef(), Not(x.getRef()));
        allSatSolver.init(expr, decls);
        makeSolutions();
    }

    @Test // (x v !x)
    public void test_5() {

        final ConstDecl<BoolType> x = Const("x", Bool());
        final ConstDecl<BoolType> y = Const("y", Bool());
        List<Decl<?>> decls = new ArrayList<>();
        decls.add(y);
        decls.add(x);

        // (x v !x)
        Expr expr = Or(x.getRef(), Not(x.getRef()));
        allSatSolver.init(expr, decls);
        makeSolutions();
    }
}
