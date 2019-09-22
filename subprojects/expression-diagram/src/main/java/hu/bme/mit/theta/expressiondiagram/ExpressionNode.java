package hu.bme.mit.theta.expressiondiagram;

import com.google.errorprone.annotations.Var;
import com.koloboke.collect.map.ObjObjCursor;
import com.koloboke.collect.map.hash.HashObjObjMap;
import com.koloboke.collect.map.hash.HashObjObjMaps;
import hu.bme.mit.theta.core.decl.ConstDecl;
import hu.bme.mit.theta.core.decl.Decl;
import hu.bme.mit.theta.core.model.ImmutableValuation;
import hu.bme.mit.theta.core.model.Valuation;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.LitExpr;
import hu.bme.mit.theta.core.type.Type;
import hu.bme.mit.theta.core.type.anytype.RefExpr;
import hu.bme.mit.theta.core.type.booltype.BoolLitExpr;
import hu.bme.mit.theta.core.type.booltype.BoolType;
import hu.bme.mit.theta.core.type.booltype.FalseExpr;
import hu.bme.mit.theta.core.type.booltype.TrueExpr;
import hu.bme.mit.theta.core.utils.ExprSimplifier;
import hu.bme.mit.theta.solver.Solver;
import hu.bme.mit.theta.solver.SolverStatus;
import hu.bme.mit.theta.solver.z3.Z3SolverFactory;

import java.util.*;

import static hu.bme.mit.theta.core.type.abstracttype.AbstractExprs.Neq;

public class ExpressionNode {
    Expr expression;
    private boolean isFinal = false;
    private boolean containsDecl = false;
    HashObjObjMap<LitExpr<? extends Type>,ExpressionNode> nextExpression = HashObjObjMaps.newUpdatableMap();
    private VariableSubstitution variableSubstitution;
    private static Stack<Cursor> cursorStack = new Stack<>();

    public ExpressionNode(VariableSubstitution vs) {
        variableSubstitution = vs;
    }

    public void setExpression(Expr e) {
        expression = e;
        if (variableSubstitution.getDecl() == null) return;
        Set<Decl<?>> vars = getDecls(e);
        if (vars.contains(variableSubstitution.getDecl())) {
            containsDecl= true;
        }
        System.out.println("Expression " + e.toString() + ", substituting " + variableSubstitution.getDecl().toString());
    }

    private DefaultLitExpr defaultLitExpr = new DefaultLitExpr();
    private ExpressionNode defaultNextNode() {
        ExpressionNode def = new ExpressionNode(variableSubstitution.next);
        if (def.variableSubstitution == null) return null;
        def.setExpression(expression);
        isFinal = true; // no more check needed, as variable is not present
        // the following two lines may be replaced with a default next node
        /*nextExpression.put(BoolLitExpr.of(true),def);
        nextExpression.put(BoolLitExpr.of(false),def);*/
        nextExpression.put(defaultLitExpr, def);
        return def;
    }

    private ExpressionNode substitute (LitExpr<? extends Type> literal) {
        if (literal != null && expression!=null)System.out.println("    Substituting " + literal.toString() + " instead of " + variableSubstitution.getDecl().toString() + " into " + expression.toString());
        //if (!containsDecl) {
        // if literal is null, or decl is not in the expression, expression goes one level below
        if (literal == null || !containsDecl) {
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

    public void calculateSatisfyingSubstitutions() {
        if (isFinal) return;
        Cursor myCursor = makeCursor();
        while (myCursor.moveNext()) {
            myCursor.getNode().calculateSatisfyingSubstitutions();
        }
    }

    private Cursor makeCursor() {
        return new Cursor(this);
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
                // cached result found
                newNode = cursor.value();
                litExpr = cursor.key();
                if (litExpr != null && litExpr != defaultLitExpr) solver.add(Neq(decl.getRef(), litExpr));
                //System.out.println("    From cache: " + litExpr.toString() + " instead of " + variableSubstitution.getDecl().toString() + " into " + expression.toString());
                return true;
            }
            cursor = null;
            if (node.isFinal || decl == null) return false;
            SolverStatus status = solver.check();
            if(status.isUnsat()) {
                // no more satisfying assignments
                node.isFinal = true;
                //System.out.println("        Finished checking " + node.expression.toString());
                return false;
            }
            Valuation model = solver.getModel();
            litExpr = model.toMap().get(decl);
            if (litExpr != null) solver.add(Neq(decl.getRef(), litExpr));
            else {
                // The solver said that the decl may be both true or false.
                // We choose it false, but later it will be checked whether true is ok.
                litExpr = FalseExpr.getInstance();
                solver.add(Neq(decl.getRef(), litExpr));
            }

            // not inspected assignment found, create new node accordingly
            newNode = node.substitute(litExpr);
            //if (newNode != null && newNode.variableSubstitution.getDecl() != null) {
            if (newNode != null) {
                return true;
            }
            node.isFinal = true;
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

    public static VariableSubstitution createDecls (List<ConstDecl<BoolType>> declList) {
        /*final ConstDecl<BoolType> ca = Const("a", Bool());
        final ConstDecl<BoolType> cb = Const("b", Bool());
        final ConstDecl<IntType> cd = Const("d", Int());
        VariableSubstitution.decls.add(ca);
        VariableSubstitution vsnull = new VariableSubstitution(null, null);
        VariableSubstitution vsb = new VariableSubstitution(vsnull,cb);
        VariableSubstitution vsa = new VariableSubstitution(vsb,ca);*/
        VariableSubstitution.decls.clear();
        VariableSubstitution.decls.addAll(declList);
        Collections.reverse(declList);
        VariableSubstitution oldVS = new VariableSubstitution(null, null);
        for (ConstDecl<BoolType> cd : declList) {
            VariableSubstitution newVS = new VariableSubstitution(oldVS, cd);
            oldVS = newVS;
        }
        return oldVS;
    }

}
