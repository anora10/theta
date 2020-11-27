package hu.bme.mit.theta.expressiondiagram.allsat;

import hu.bme.mit.theta.common.logging.ConsoleLogger;
import hu.bme.mit.theta.common.logging.Logger;
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
import hu.bme.mit.theta.expressiondiagram.utils.DiagramToGraphUtil;

import java.io.IOException;
import java.util.*;

public class BddAllSatSolver implements AllSatSolver{
    ExpressionNode node = null;
    VariableSubstitution vs = null;
    SolutionCursor solutionCursor;
    boolean isFinal;

    private static List<String> variableOrder = null;

    GraphvizWriter graphvizWriter = GraphvizWriter.getInstance();


    /**
     * Set order of current variables
     *
     * @param decls list of decls
     */
    public void setVariables(List<? extends Decl> decls) {
        // if no custom variable order is set
        if (variableOrder == null) {
            vs = ExpressionNode.createDecls(decls, false);
        } else {
            List<Decl> orderedDecl = new ArrayList();
            for (String declName : variableOrder) {
                for (Decl decl : decls) {
                    if (declName.equals(decl.getName())) orderedDecl.add(decl);
                }
            }
            vs = ExpressionNode.createDecls(orderedDecl, false);
        }
    }

    /**
     * Set order of all possible variables
     *
     * @param list list of variables in REVERSED order
     */
    public static void setVariableOrder(List<String> list) {
        variableOrder = list;
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

    /**
     * Init solver
     *
     * @param expr original expression
     * @param decls level variable list
     */
    @Override
    public void init(Expr<?> expr, List<? extends Decl> decls) {
        setVariables(decls);
        node = new ExpressionNode(vs, expr);
        solutionCursor = new SolutionCursor(node);
        isFinal = expr.equals(FalseExpr.getInstance());
    }

    /**
     * Init solver, variables are set automatically
     *
     * @param expr original expression
     */
    @Override
    public void init(Expr<?> expr) {
        List<? extends Decl> decls = new ArrayList<>(ExpressionNode.getDecls(expr));
        init(expr, decls);
    }

    /**
     * Get next solution as map
     *
     * @return solution map
     */
    @Override
    public HashMap<Decl<?>, LitExpr<?>> nextMap() {
        isFinal = ! solutionCursor.moveNext();
        return isFinal ? null : solutionCursor.getSolutionMap();
    }

    /**
     * Check if there are solutions left
     *
     * @return false if all solutions are found
     */
    @Override
    public boolean hasNext() {
        return !isFinal;
    }

    /**
     * Get next solution valuation
     *
     * @return next solution
     */
    @Override
    public Valuation next() {
        isFinal = ! solutionCursor.moveNext();
        if (isFinal) return null;
        Valuation valuation = solutionCursor.getSolutionValuation();
        if (valuation.toMap().isEmpty()) isFinal = true;
        return valuation;
    }

    /**
     * Save substitution diagram as a .dot file
     */
    @Override
    public void writeGraph() {
        if (DiagramToGraphUtil.getVisualize() == true) {
            Graph graph = DiagramToGraphUtil.toGraph(node);
            try {
                graphvizWriter.writeFileAutoConvert(graph, graph.getId() + ".dot");
            } catch (final Throwable ex) {
                new ConsoleLogger(hu.bme.mit.theta.common.logging.Logger.Level.RESULT).write(Logger.Level.RESULT, ex.getMessage());
            }
        }
    }

}
