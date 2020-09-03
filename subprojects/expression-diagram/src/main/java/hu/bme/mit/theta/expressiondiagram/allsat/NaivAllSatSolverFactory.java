package hu.bme.mit.theta.expressiondiagram.allsat;

import hu.bme.mit.theta.solver.z3.Z3SolverFactory;

public class NaivAllSatSolverFactory implements AllSatSolverFactory{
    private static final NaivAllSatSolverFactory INSTANCE;

    static {
        INSTANCE = new NaivAllSatSolverFactory();
    }

    public static NaivAllSatSolverFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public AllSatSolver createSolver() {
        return new NaivAllSatSolver();
    }
}
