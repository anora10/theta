package hu.bme.mit.theta.expressiondiagram.allsat;

import hu.bme.mit.theta.common.visualization.Graph;
import hu.bme.mit.theta.common.visualization.writer.GraphvizWriter;
import hu.bme.mit.theta.core.decl.Decl;
import hu.bme.mit.theta.core.model.Valuation;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.LitExpr;
import hu.bme.mit.theta.core.type.booltype.FalseExpr;
import hu.bme.mit.theta.core.utils.VarIndexing;
import hu.bme.mit.theta.expressiondiagram.ExpressionNode;
import hu.bme.mit.theta.expressiondiagram.SolutionCursor;
import hu.bme.mit.theta.expressiondiagram.VariableSubstitution;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class BddAllSatSolver implements AllSatSolver{
    ExpressionNode node = null;
    VariableSubstitution vs = null;
    SolutionCursor solutionCursor;
    boolean isFinal;

    GraphvizWriter graphvizWriter = GraphvizWriter.getInstance();

    public void setVariables(List<? extends Decl> decls) {
        vs = ExpressionNode.createDecls(decls, false);
    }

    @Override
    public void setK(HashMap<Decl<?>, Integer> ks) {
        VariableSubstitution tempVS = vs;
        for (Decl<?> d : ks.keySet()) {
            while (tempVS != null && ! tempVS.getDecl().equals(d)) tempVS = tempVS.getNext();
            if (tempVS == null) continue;
            tempVS.setMaxSize(ks.get(d));
        }
    }

    @Override
    public void setK(int k) {
        VariableSubstitution tempVS = vs;
        while (tempVS != null) {
            tempVS.setMaxSize(k);
            tempVS = tempVS.getNext();
        }
    }

    @Override
    public void init(Expr<?> expr, List<? extends Decl> decls) {
        setVariables(decls);
        node = new ExpressionNode(vs, expr);
        solutionCursor = new SolutionCursor(node);
        isFinal = expr.equals(FalseExpr.getInstance());
    }

    @Override
    public void init(Expr<?> expr) {
        List<? extends Decl> decls = new ArrayList<>(ExpressionNode.getDecls(expr));
        init(expr, decls);
    }

    @Override
    public HashMap<Decl<?>, LitExpr<?>> nextMap() {
        isFinal = ! solutionCursor.moveNext();
        return isFinal ? null : solutionCursor.getSolutionMap();
    }

    @Override
    public boolean hasNext() {
        return !isFinal;
    }

    @Override
    public Valuation next() {
        isFinal = ! solutionCursor.moveNext();
        if (isFinal) return null;
        Valuation valuation = solutionCursor.getSolutionValuation();
        if (valuation.toMap().isEmpty()) isFinal = true;
        return valuation;
    }

    static int cnt = 0;
    @Override
    public void writeGraph() {
        Graph graph = node.toGraph();
        try {
            graphvizWriter.writeFile(graph, Integer.toString(cnt), GraphvizWriter.Format.PNG);
        } catch (Exception e) {
            //TODO handle error
        }
        cnt++;
    }
}
