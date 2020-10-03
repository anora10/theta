package hu.bme.mit.theta.expressiondiagram;

import hu.bme.mit.theta.core.decl.ConstDecl;
import hu.bme.mit.theta.core.decl.Decl;
import hu.bme.mit.theta.core.model.Valuation;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.LitExpr;
import hu.bme.mit.theta.core.type.booltype.BoolExprs;
import hu.bme.mit.theta.core.type.booltype.BoolType;
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
    }

    @Test // (a v !b) ^ (b v c)
    public void test_1() {

        final List<ConstDecl<?>> actLits = new ArrayList<>();
        final ConstDecl<BoolType> ca = Const("a", Bool());
        final ConstDecl<BoolType> cb = Const("b", Bool());
        final ConstDecl<BoolType> cc = Const("c", Bool());

        actLits.add(cc);
        actLits.add(cb);
        actLits.add(ca);

        // (a v !b) ^ (b v c)
        Expr<?> expr = BoolExprs.And(Or(ca.getRef(), Not(cb.getRef())), Or(cb.getRef(), cc.getRef()));
        //Expr expr = True();
        allSatSolver.init(expr, actLits);

        makeSolutions();

    }
}
