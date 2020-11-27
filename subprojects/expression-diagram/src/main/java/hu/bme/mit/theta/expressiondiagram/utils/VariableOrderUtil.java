package hu.bme.mit.theta.expressiondiagram.utils;

import hu.bme.mit.theta.common.logging.ConsoleLogger;
import hu.bme.mit.theta.common.logging.Logger;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class VariableOrderUtil {

    public static List<String> loadVariableOrder(String variableOrder) {
        if (variableOrder == null) return null;
        List<String> result = new ArrayList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(variableOrder));
            String line = reader.readLine();
            while (line != null) {
                result.add(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            new ConsoleLogger(Logger.Level.MAINSTEP).write(Logger.Level.MAINSTEP, "Failed to read variable order with " + e.getMessage());
        }
        return result;
    }
}
