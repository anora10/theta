package hu.bme.mit.theta.expressiondiagram;

import com.koloboke.collect.map.ObjObjCursor;
import com.koloboke.collect.map.hash.HashObjObjMap;
import com.koloboke.collect.map.hash.HashObjObjMaps;
import hu.bme.mit.theta.core.decl.Decl;
import hu.bme.mit.theta.core.model.ImmutableValuation;
import hu.bme.mit.theta.core.model.Valuation;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.LitExpr;
import hu.bme.mit.theta.core.type.Type;
import hu.bme.mit.theta.core.type.anytype.RefExpr;
import hu.bme.mit.theta.core.utils.ExprSimplifier;
import hu.bme.mit.theta.solver.Solver;
import hu.bme.mit.theta.solver.SolverStatus;
import hu.bme.mit.theta.solver.z3.Z3SolverFactory;

import java.util.*;

import static hu.bme.mit.theta.core.type.abstracttype.AbstractExprs.Neq;
import static hu.bme.mit.theta.core.utils.ExprUtils.getVars;

public class ExpressionNode {
    Expr expression;
    boolean isFinal = false;
    boolean containsDecl = false;
    HashObjObjMap<LitExpr<? extends Type>,ExpressionNode> nextExpression = HashObjObjMaps.newUpdatableMap();
    VariableSubstitution variableSubstitution;

    ExpressionNode(VariableSubstitution vs) {
        variableSubstitution = vs;
    }

    void setExpression (Expr e) {
        expression = e;
        if (variableSubstitution.getDecl() == null) return;
        Set<Decl<?>> vars = getDecls(e);
        if (vars.contains(variableSubstitution.getDecl())) {
            containsDecl= true;
        }
        System.out.println("Expression " + e.toString() + ", substituting " + variableSubstitution.getDecl().toString());
    }

    ExpressionNode defaultNextNode() {
        ExpressionNode def = new ExpressionNode(variableSubstitution.next);
        if (def.variableSubstitution == null) return null;
        def.setExpression(expression);
        return def;
    }



    ExpressionNode substitute (LitExpr<? extends Type> literal) {
        // if literal is null, expression goes one level below
        if (literal != null && expression!=null)System.out.println("    Substituting " + literal.toString() + " instead of " + variableSubstitution.getDecl().toString() + " into " + expression.toString());
        if (containsDecl == false) {
            return defaultNextNode();
        }
        // get variable to substitute
        if (nextExpression.containsKey(literal)) return nextExpression.get(literal);
        Decl decl = variableSubstitution.getDecl();
        ImmutableValuation valuation = ImmutableValuation.builder().put(decl, literal).build();
        // resultingExpression: expression after substitution
        Expr<? extends Type> resultingExpression = ExprSimplifier.simplify(expression, valuation);
        ExpressionNode newNode = variableSubstitution.checkIn(resultingExpression);
        nextExpression.put(literal, newNode);
        return newNode;
    }

    void getSatisfyingSubstitutions() {
        if (isFinal) return;
        Cursor cursor = new Cursor(this);
        while (cursor.moveNext()) {
            cursor.getNode().getSatisfyingSubstitutions();
            //System.out.println("--- " + expression.toString() + " ---");
            //System.out.println("--- " + cursor.getLiteral().toString() + "   " + cursor.getNode().expression.toString() + " ---");
        }
    }


    class Cursor {
        private ExpressionNode node, newNode;
        private Solver solver;
        private LitExpr litExpr;
        private Decl decl;
        private ObjObjCursor<LitExpr<? extends Type>, ExpressionNode> cursor = nextExpression.cursor();

        Cursor(ExpressionNode n) {
            node = n;
            solver = Z3SolverFactory.getInstace().createSolver();
            solver.add(n.expression);
            decl = node.variableSubstitution.getDecl();
            // TODO: ne legyen ennyi solver
        }

        ExpressionNode getNode() {
            // return lastly calculated node
            return newNode;
        }

        LitExpr<? extends Type> getLiteral() {
            // return lastly calculated value
            return litExpr;
        }

        boolean moveNext() { // gives false, if no more satisfying assignments can be found
            // litExpr will be the calculated value
            // if node is final, there is no next to move to
            if (cursor != null && cursor.moveNext()) {
                newNode = cursor.value();
                litExpr = cursor.key();
                if (litExpr != null) solver.add(Neq(decl.getRef(), litExpr));
                System.out.println("    From cache: " + litExpr.toString() + " instead of " + variableSubstitution.getDecl().toString() + " into " + expression.toString());
                return true;
            }
            cursor = null;
            if (node.isFinal) return false;
            SolverStatus status = solver.check();
            if(status.isUnsat()) {
                // no more satisfying assignments
                node.isFinal = true;
                System.out.println("        Finished checking " + node.expression.toString());
                return false;
            }
            Valuation model = solver.getModel();
            litExpr = model.toMap().get(decl);
            if (litExpr != null) solver.add(Neq(decl.getRef(), litExpr));
            // not inspected assignment found, create new node accordingly
            newNode = node.substitute(litExpr);
            if (newNode != null && newNode.variableSubstitution.getDecl() != null) {
                //newNode.getSatisfyingSubstitutions();
                return true;
            }
            return false;
        }
    }

    void DFS(int tabnum) {
        String s = String.format("%1$"+tabnum+"s", "");
        System.out.println("### " + s + expression.toString());
        tabnum++;
        long i = 0;
        for (Expr key : nextExpression.keySet()) {
            nextExpression.get(key).DFS(tabnum+1);
        }
    }

    private static void collectDecls(final Expr<?> expr, final Collection<Decl<?>> collectTo) {
        if (expr instanceof RefExpr) {
            final RefExpr<?> refExpr = (RefExpr<?>) expr;
            final Decl<?> decl = refExpr.getDecl();
            collectTo.add(decl);
            return;
        }
        expr.getOps().forEach(op -> collectDecls(op, collectTo));
    }

    private static Set<Decl<?>> getDecls(final Expr<?> expr) {
        final Set<Decl<?>> decls = new HashSet<>();
        collectDecls(expr, decls);
        return decls;
    }
}
