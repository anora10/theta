package hu.bme.mit.theta.expressiondiagram.logger;

import hu.bme.mit.theta.common.logging.ConsoleLogger;
import hu.bme.mit.theta.common.logging.Logger;

public interface ExpressionDiagramLogger {
    ConsoleLogger logger = new ConsoleLogger(Logger.Level.DETAIL);

    public void write(String text, Object obj);
    public void addTab();
    public void subTab();
}
