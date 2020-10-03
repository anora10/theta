package hu.bme.mit.theta.expressiondiagram.logger;

import hu.bme.mit.theta.common.logging.Logger;

import java.util.HashMap;
import java.util.Map;

public class SmartLogger implements ExpressionDiagramLogger{

    Map<Object, Integer> objects = new HashMap<>();
    int tabs = 0;

    @Override
    public void write(String text, Object obj) {
        if (! objects.containsKey(obj)) {
            objects.put(obj, objects.size());
        }
        String tab = "";
        for (int i = 0; i < tabs; i++) {
            //logger.write(Logger.Level.DETAIL, "\t");
            tab += "\t";
        }
        //logger.write(Logger.Level.DETAIL, obj.getClass().toString() + objects.get(obj) +" text");
        System.out.println(tab + objects.get(obj) +" "+ text);
    }

    @Override
    public void addTab() {
        tabs++;
    }

    @Override
    public void subTab() {
        if (tabs > 0) tabs--;
    }
}
