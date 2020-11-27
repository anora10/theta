package hu.bme.mit.theta.expressiondiagram.utils;

public class SolverCallUtil {
    private static Integer solverCalls = 0;

    public static void resetSolverCalls() {
        solverCalls = 0;
    }

    public static void increaseSolverCalls() {
        solverCalls++;
    }

    public static Integer getSolverCalls() {
        return solverCalls;
    }
}
