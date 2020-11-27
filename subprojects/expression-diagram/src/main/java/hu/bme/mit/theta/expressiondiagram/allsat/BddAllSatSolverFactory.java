package hu.bme.mit.theta.expressiondiagram.allsat;

import hu.bme.mit.theta.common.logging.ConsoleLogger;
import hu.bme.mit.theta.expressiondiagram.utils.SolverCallUtil;

public class BddAllSatSolverFactory implements AllSatSolverFactory{
    private static final BddAllSatSolverFactory INSTANCE;

    static {
        INSTANCE = new BddAllSatSolverFactory();
    }

    public static BddAllSatSolverFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public AllSatSolver createSolver() {
        //SolverCallUtil.resetSolverCalls();
        return new BddAllSatSolver();
    }
}
