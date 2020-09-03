package hu.bme.mit.theta.expressiondiagram.allsat;

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
        return new BddAllSatSolver();
    }
}
