package hu.bme.mit.theta.expressiondiagram;

import com.koloboke.collect.map.ObjObjCursor;
import hu.bme.mit.theta.core.decl.Decl;
import hu.bme.mit.theta.core.model.Valuation;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.LitExpr;
import hu.bme.mit.theta.core.type.Type;
import hu.bme.mit.theta.core.type.booltype.FalseExpr;
import hu.bme.mit.theta.core.type.booltype.TrueExpr;
import hu.bme.mit.theta.solver.SolverStatus;

import java.util.HashMap;
import java.util.Iterator;

import static hu.bme.mit.theta.core.type.abstracttype.AbstractExprs.Eq;
import static hu.bme.mit.theta.core.type.abstracttype.AbstractExprs.Neq;

class NodeCursor {
    public static int megoldas = 0;
    private ExpressionNode node, newNode;
    private LitExpr litExpr;
    public Decl decl;
    MapCursor mapCursor;
    static HashMap<Decl, LitExpr<? extends Type>> solutionMap = new HashMap<>();
    public static boolean changed = false;


    /**
     * Constructor for cursor that searces for satisfying substitutions using a DFS method
     *
     * @param n node
     */
    NodeCursor(ExpressionNode n) {
        node = n;
        decl = node.variableSubstitution.getDecl();
        mapCursor = new MapCursor(node);
    }

    /**
     * Return lastly calculated node
     *
     * @return node
     */
    ExpressionNode getNode() {
        return newNode;
    }

    /**
     * Return lastly calculated value
     *
     * @return value
     */
    LitExpr<? extends Type> getLiteral() {
        return litExpr;
    }

    void getCachedResult() {
        newNode = mapCursor.value();
        litExpr = mapCursor.key();
    }

    boolean getNewResult() {
        SolverStatus status = node.solver.check();
        if (status.isUnsat()) {
            // no more satisfying assignments
            node.isFinal = true;
            return false;
        }
        Valuation model = node.solver.getModel();
        /// save model as queue
        node.modelMap = model.toMap();
        boolean result = saveSolverSolution();
        return  result;
    }

    boolean saveSolverSolution() {
        litExpr = node.modelMap.get(decl);
        if (litExpr != null) {}
        else {
            // The solver said that the decl may be both true or false.
            // We choose it false, but later it will be checked whether true is ok.
            litExpr = FalseExpr.getInstance();
            //solver.add(Neq(decl.getRef(), litExpr));
        }

        // not inspected assignment found, create new node accordingly
        newNode = node.substitute(litExpr);
        if (newNode == null || newNode.expression.equals(FalseExpr.getInstance())) return false;
        if (newNode.expression.equals(TrueExpr.getInstance())) return true;
        if (newNode != null && !newNode.isFinal) {
            newNode.makeCursor().saveSolverSolution();
        }
        return true;
    }

    private void doBeforeNewResult() {
        for (Decl d : node.variableSubstitution.decls) {
            if (d.equals(decl)) break;
            ExpressionNode.solver.add(Eq(d.getRef(), solutionMap.get(d)));
        }
    }

    private void putInMap() {
        if (solutionMap.get(decl) != mapCursor.key())
            changed = true;
        solutionMap.put(decl, mapCursor.key());
    }

    /**
     * Search for next satisfying substitution
     * litExpr will be the calculated value
     * if node is final, there is no next to move to
     *
     * @return false, if no more satisfying assignments can be found
     */
    boolean moveNext() { // gives false, if no more satisfying assignments can be found
        if (node.expression.equals(TrueExpr.getInstance())) return true;
        ExpressionNode nextNode = mapCursor.value();
        if (!mapCursor.hasPrevious() && mapCursor.key()== null) // nodeexpression vizsgalatanak kezdeten vagyunk
            ExpressionNode.solver.push();
        if (nextNode != null) { // megprobal alatta keresni
            if ( (changed || !nextNode.expression.equals(TrueExpr.getInstance())) && nextNode.nodeCursor.moveNext()) {
                // van alatta még megoldás
                return true;
            }
            // nincs alatta megoldas, a mapCursor tovabb fog lepni
            ExpressionNode.solver.add(Neq(decl.getRef(), mapCursor.key()));
        }
        if (mapCursor.hasNext()) {
            if (mapCursor.key() != null) {
                ExpressionNode.solver.add(Neq(decl.getRef(), mapCursor.key()));
            }
            mapCursor.moveNext();
            // resetall? elvileg nem kell
            nextNode = mapCursor.value();
            if (nextNode.nodeCursor.moveNext()) {
                // a mapcursor lépett egyet, a mutatott Node első megoldását lementette a hívott moveNext
                putInMap();
                return true;
            }
            assert (0==1); // ide nem kéne jutni
            return false;
        }
        // uj megoldas
        doBeforeNewResult();
        ExpressionNode.solver.push();
        boolean result = getNewResult();
        ExpressionNode.solver.pop();
        if (!result) {
            node.isFinal = true;
            ExpressionNode.solver.pop();
            mapCursor.reset(); // hogy később ha ebbe a node-ba lépünk, újrakezdje
            mapCursor.lastLitExpr = null; //ezt nagyon nem itt kéne
            return false; // nincs több megoldás
        }
        // a getNewResult beepitette az uj megoldast, azt most kinyerjuk
        if (mapCursor.key() != null) {
            ExpressionNode.solver.add(Neq(decl.getRef(), mapCursor.key())); ///utana!
        }
        mapCursor.moveNext();
        nextNode = mapCursor.value();
        putInMap();
        if (nextNode != null) {
            if (nextNode.nodeCursor.moveNext()) {
                return true;
            }
        }
         assert (0==1); //ide se kéne jutni
        return false;
    }

    class MapCursor {
        //private ObjObjCursor<LitExpr<? extends Type>, ExpressionNode> ;
        private Iterator<LitExpr<? extends Type>> it;
        private boolean changed = false;
        LitExpr lastLitExpr = null;
        boolean hasPrev = false;
        MapCursor(ExpressionNode n) {
            it = n.nextExpression.keySet().iterator();
        }

        public boolean hasNext() {
            if (changed) { // reset state-before-change
                reset();
                if (lastLitExpr != null) {
                    while (it.hasNext() && !it.next().equals(lastLitExpr)) {}
                }
            }
            if (it.hasNext()) return true;
            return false;
        }

        public boolean moveNext() {
            hasPrev = true;
            if (changed) { // reset state-before-change
                reset();
                if (lastLitExpr != null) {
                    while (it.hasNext() && !it.next().equals(lastLitExpr)) {}
                }
            }
            if (it.hasNext())  {
                lastLitExpr = it.next();
                return true;
            }
            // vegigneztuk a map-et
            reset();
            lastLitExpr = null;
            return false;
        }

        LitExpr key() {
            return lastLitExpr;
        }

        ExpressionNode value() {
            if (lastLitExpr == null) return null;
            return node.nextExpression.get(lastLitExpr);
        }

        private void reset() {
            it = node.nextExpression.keySet().iterator();
            changed = false;
            hasPrev = false;
        }

        public void setChanged(boolean changed) {
            this.changed = changed;
        }

        public boolean hasPrevious() {
            return hasPrev;
        }
    }
}