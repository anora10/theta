package hu.bme.mit.theta.expressiondiagram;

import hu.bme.mit.theta.core.decl.ConstDecl;
import hu.bme.mit.theta.core.decl.Decl;
import hu.bme.mit.theta.core.model.Valuation;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.LitExpr;
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
import static hu.bme.mit.theta.core.type.booltype.SmartBoolExprs.And;
import static hu.bme.mit.theta.core.type.inttype.IntExprs.Int;

public class AllSatSolverTest {

        @Test // (d <= 3)
    public void test_1() {
        final ConstDecl<IntType> x = Const("x", Int());
        final ConstDecl<IntType> y = Const("y", Int());
        List<Decl<?>> decls = new ArrayList<>();
            decls.add(x);
            decls.add(y);
        // (d <= 3)
        Expr expr = And(Leq(Int(0), x.getRef()), Leq(x.getRef(), Int(5)));
        //Expr expr = True();
        AllSatSolver allSatSolver = BddAllSatSolverFactory.getInstance().createSolver();
            allSatSolver.init(expr, decls);

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
}
