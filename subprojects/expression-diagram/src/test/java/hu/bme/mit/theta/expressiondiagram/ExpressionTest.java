package hu.bme.mit.theta.expressiondiagram;

import static hu.bme.mit.theta.core.decl.Decls.Const;
import static hu.bme.mit.theta.core.type.booltype.BoolExprs.*;
import static hu.bme.mit.theta.core.type.inttype.IntExprs.Add;
import static hu.bme.mit.theta.core.type.inttype.IntExprs.Eq;
import static hu.bme.mit.theta.core.type.inttype.IntExprs.Int;
import static org.junit.Assert.*;

import hu.bme.mit.theta.core.decl.ConstDecl;
import hu.bme.mit.theta.core.decl.Decl;
import hu.bme.mit.theta.core.decl.VarDecl;
import hu.bme.mit.theta.core.model.ImmutableValuation;
import hu.bme.mit.theta.core.model.Valuation;
import hu.bme.mit.theta.core.type.LitExpr;
import hu.bme.mit.theta.core.type.Type;
import hu.bme.mit.theta.core.type.booltype.BoolExprs;
import hu.bme.mit.theta.core.type.booltype.BoolLitExpr;
import hu.bme.mit.theta.core.type.booltype.BoolType;
import hu.bme.mit.theta.core.type.inttype.IntLitExpr;
import hu.bme.mit.theta.core.type.inttype.IntType;
import hu.bme.mit.theta.solver.Solver;
import hu.bme.mit.theta.solver.SolverFactory;
import hu.bme.mit.theta.solver.SolverStatus;
import org.junit.Test;
import hu.bme.mit.theta.solver.z3.Z3SolverFactory;

import java.util.Map;

public class ExpressionTest {
    @Test
    public void test() {
        //assertEquals(0, 1);
        final ConstDecl<BoolType> decl1 = Const("a", Bool());
        final ConstDecl<BoolType> decl2 = Const("a", Bool());
        LitExpr<BoolType> lit = BoolLitExpr.of(false);
        ImmutableValuation val = ImmutableValuation.builder().put(decl1, lit).build();
        SolverFactory solverFactory = Z3SolverFactory.getInstace();
        Solver solver = solverFactory.createSolver();
        solver.add(And(decl1.getRef(), True()));
        SolverStatus status = solver.check();
        System.out.println(val.toString());
        System.out.println(status.isSat());
        assertEquals( true, status.isSat()) ;
        Valuation model = solver.getModel();
        System.out.println(model.toString());
    }
}
