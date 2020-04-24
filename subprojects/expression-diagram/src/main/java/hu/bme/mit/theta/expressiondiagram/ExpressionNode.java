package hu.bme.mit.theta.expressiondiagram;

import com.koloboke.collect.map.hash.HashObjObjMap;
import com.koloboke.collect.map.hash.HashObjObjMaps;
import hu.bme.mit.theta.core.decl.ConstDecl;
import hu.bme.mit.theta.core.decl.Decl;
import hu.bme.mit.theta.core.model.ImmutableValuation;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.LitExpr;
import hu.bme.mit.theta.core.type.Type;
import hu.bme.mit.theta.core.type.anytype.RefExpr;
import hu.bme.mit.theta.core.type.booltype.BoolType;
import hu.bme.mit.theta.core.type.booltype.FalseExpr;
import hu.bme.mit.theta.core.type.booltype.TrueExpr;
import hu.bme.mit.theta.core.utils.ExprSimplifier;

import java.util.*;


public class ExpressionNode {
    Expr expression;
    boolean isFinal = false;
    private boolean containsDecl = false;
    HashObjObjMap<LitExpr<? extends Type>,ExpressionNode> nextExpression = HashObjObjMaps.newUpdatableMap();
    VariableSubstitution variableSubstitution;
    private static Stack<NodeCursor> cursorStack = new Stack<>();

    /**
     * Constructor for ExpressionNode, sequence of variable subtitution is given
     *
     * @param vs contains the variable subtitution order
     */
    public ExpressionNode(VariableSubstitution vs, Expr expr) {
        variableSubstitution = vs;
        setExpression(expr);
    }

    /**
     * Set expression of Node, and check whether it contains the next literal which will be substituted
     *
     * @param e expression to be set
     */
    private void setExpression(Expr e) {
        expression = e;
        if (variableSubstitution == null || variableSubstitution.getDecl() == null) return;
        Set<Decl<?>> vars = getDecls(e);
        if (vars.contains(variableSubstitution.getDecl())) {
            containsDecl= true;
        }
        if ( expression.equals(TrueExpr.getInstance()) || expression.equals(FalseExpr.getInstance()) )
            isFinal = true;
    }

    /**
     * Substitute literal in the expression of the node, or if variable to substitute is not present, return default next node
     *
     * @param literal substitution value
     * @return node with resulting expression
     */
    ExpressionNode substitute (LitExpr<? extends Type> literal) {
        // if literal is null, or decl is not in the expression, expression goes one level below
        ExpressionNode newNode;
        if (literal == null || !containsDecl) {
            // literal value has no effect on resulting expression
            newNode = variableSubstitution.next.checkIn(expression);
            isFinal = true;
        } else {
            // get variable to substitute
            if (nextExpression.containsKey(literal))
                return nextExpression.get(literal);
            Decl decl = variableSubstitution.getDecl();
            ImmutableValuation valuation = ImmutableValuation.builder().put(decl, literal).build();
            // resultingExpression: expression after substitution
            Expr<? extends Type> resultingExpression = ExprSimplifier.simplify(expression, valuation);
            newNode = variableSubstitution.next.checkIn(resultingExpression);
        }
        nextExpression.put(literal, newNode);
        return newNode;
    }

    /**
     * Substitute multiple literals in the expression of the node one by one, return node after first substitution
     *
     * @param literals map of substitution values
     * @return node after first substitution
     */
    // TODO nem jo, a defaulttal kell szorakozni
    ExpressionNode substituteAll(HashMap<Decl, LitExpr<? extends Type>> literals) {
        if (variableSubstitution == null || variableSubstitution.getDecl() == null) return null;
        if (expression.equals(TrueExpr.getInstance()) || expression.equals(FalseExpr.getInstance())) return null;
        Decl decl = variableSubstitution.getDecl();
        LitExpr litExpr;
        if (literals.containsKey(decl)) {
            litExpr = literals.get(decl);
        } else {
            litExpr = DefaultLitExpr.getInstance();
        }
        ExpressionNode newNode = substitute(litExpr);
        newNode.substituteAll(literals);
        return newNode;
    }

    /**
     * Return a Cursor instance
     *
     * @return cursor instance
     */
    NodeCursor makeCursor() {
        return new NodeCursor(this);
    }

    /**
     * Collect decls occurring in expression
     *
     * @param expr expression
     * @param collectTo where to collect
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
     * @param expr expression
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
     * @param declList list of decls
     * @param reverse if true, decl order will be reversed
     * @return VariableSubstitution, that will be needed in constructor of ExpressionNode
     */
    public static VariableSubstitution createDecls (List<ConstDecl<BoolType>> declList, boolean reverse) {
        /*final ConstDecl<BoolType> ca = Const("a", Bool());
        final ConstDecl<BoolType> cb = Const("b", Bool());
        final ConstDecl<IntType> cd = Const("d", Int());
        VariableSubstitution.decls.add(ca);
        VariableSubstitution vsnull = new VariableSubstitution(null, null);
        VariableSubstitution vsb = new VariableSubstitution(vsnull,cb);
        VariableSubstitution vsa = new VariableSubstitution(vsb,ca);*/
        VariableSubstitution.decls.clear();
        VariableSubstitution.decls.addAll(declList);

         if (reverse) Collections.reverse(declList);
        VariableSubstitution oldVS = new VariableSubstitution(null, null);
        for (ConstDecl<BoolType> cd : declList) {
            oldVS = new VariableSubstitution(oldVS, cd);
        }
        return oldVS;
    }

}
