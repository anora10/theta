package hu.bme.mit.theta.expressiondiagram;

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
    boolean isFinal = false;
    private boolean containsDecl = false;
    HashObjObjMap<LitExpr<? extends Type>,ExpressionNode> nextExpression = HashObjObjMaps.newUpdatableMap();
    VariableSubstitution variableSubstitution;
    private static Stack<NodeCursor> cursorStack = new Stack<>();
    static Map<Decl<?>, LitExpr<?>> modelMap = null;

    /**
     * Constructor for ExpressionNode, sequence of variable subtitution is given
     *
     * @param vs contains the variable subtitution order
     */
    public ExpressionNode(VariableSubstitution vs) {
        variableSubstitution = vs;
    }

    /**
     * Set expression of Node, and check whether it contains the next literal which will be substituted
     *
     * @param e
     */
    public void setExpression(Expr e) {
        expression = e;
        if (variableSubstitution.getDecl() == null) return;
        Set<Decl<?>> vars = getDecls(e);
        if (vars.contains(variableSubstitution.getDecl())) {
            containsDecl= true;
        }
        if (expression.equals(TrueExpr.getInstance())) isFinal = true;
        //System.out.println("Expression " + e.toString() + ", substituting " + variableSubstitution.getDecl().toString());
    }

    /**
     * Default LitExpr is used when true and false substitution gives the same result
     *
     * @return default ExpressionNode
     */
    private ExpressionNode defaultNextNode() {
        ExpressionNode def = new ExpressionNode(variableSubstitution.next);
        if (def.variableSubstitution == null) return null;
        def.setExpression(expression);
        isFinal = true; // no more check needed, as variable is not present
        // the following two lines may be replaced with a default next node
        /*nextExpression.put(BoolLitExpr.of(true),def);
        nextExpression.put(BoolLitExpr.of(false),def);*/
        nextExpression.put(new DefaultLitExpr(), def);
        return def;
    }

    /**
     * Substitute literal in the expression of the node, or if variable to substitute is not present, return default next node
     *
     * @param literal substitution value
     * @return node with resulting expression
     */
    ExpressionNode substitute(LitExpr<? extends Type> literal) {
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

    /**
     * Calculate satisfying substitutions for the expression recursively
     *
     */
    public void calculateSatisfyingSubstitutions() {
        if (isFinal) return;
        NodeCursor myNodeCursor = makeCursor();
        solver.push();
        while (myNodeCursor.moveNext()) {
            myNodeCursor.getNode().calculateSatisfyingSubstitutions();
            solver.add(Neq(myNodeCursor.decl.getRef(), myNodeCursor.getLiteral()));
        }
        solver.pop();
    }

    /**
     * Return a Cursor instance
     *
     * @return cursor instance
     */
    NodeCursor makeCursor() {
        return new NodeCursor(this);
    }

    static Solver solver = Z3SolverFactory.getInstace().createSolver();

    /**
     * Initiate solver with expression and push
     *
     * @param e expression
     */
    public static void initiateSolver(Expr e) {
        solver.reset();
        solver.add(e);
        solver.push();
    }



    /**
     * DFS for testing
     *
     */
    void DFS(int tabnum) {
        String s = String.format("%1$"+tabnum+"s", "");
        System.out.println("%%%" + s + expression.toString());
        tabnum++;
        long i = 0;
        for (Expr key : nextExpression.keySet()) {
            nextExpression.get(key).DFS(tabnum+1);
        }
    }

    /**
     * Collect decls occurring in expression
     *
     * @param expr
     * @param collectTo
     */
    private static void collectDecls(final Expr<?> expr, final Collection<Decl<?>> collectTo) {
        if (expr instanceof RefExpr) {
            final RefExpr<?> refExpr = (RefExpr<?>) expr;
            final Decl<?> decl = refExpr.getDecl();
            collectTo.add(decl);
            return;
        }
        expr.getOps().forEach(op -> collectDecls(op, collectTo));
    }

    /**
     * Get decls from an expression
     *
     * @param expr
     * @return set of decls
     */
    private static Set<Decl<?>> getDecls(final Expr<?> expr) {
        final Set<Decl<?>> decls = new HashSet<>();
        collectDecls(expr, decls);
        return decls;
    }

    /**
     * Create substitution order for decls
     *
     * @return VariableSubstitution, that will be needed in constructor of ExpressionNode
     */
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
