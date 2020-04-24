package hu.bme.mit.theta.expressiondiagram;

import com.koloboke.collect.map.ObjObjCursor;
import hu.bme.mit.theta.core.decl.Decl;
import hu.bme.mit.theta.core.model.Valuation;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.LitExpr;
import hu.bme.mit.theta.core.type.Type;
import hu.bme.mit.theta.core.type.booltype.FalseExpr;
import hu.bme.mit.theta.core.type.booltype.TrueExpr;
import hu.bme.mit.theta.solver.Solver;
import hu.bme.mit.theta.solver.SolverStatus;
import hu.bme.mit.theta.solver.z3.Z3SolverFactory;

import java.util.Iterator;
import java.util.Map;

class NodeCursor {
    public static int megoldas = 0;
    ExpressionNode node;
    private ExpressionNode newNode; // new Node
    private LitExpr literal; // new Literal
    ObjObjCursor<LitExpr<? extends Type>, ExpressionNode> mapCursor;
    //public Decl decl;
    //public static Decl lastPutInMap;
    //public static Decl resultDecl;
    //static HashMap<Decl, LitExpr<? extends Type>> solutionMap = new HashMap<>();
    //public static boolean changed = false;

    static Solver solver = Z3SolverFactory.getInstace().createSolver();
    private static Map<Decl<?>, LitExpr<?>> modelMap = null;

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
     * Constructor for cursor that searces for satisfying substitutions using a DFS method
     *
     * @param n node
     */
    NodeCursor(ExpressionNode n) {
        node = n;
        mapCursor = node.nextExpression.cursor();
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
        return literal;
    }

    /**
     * Search for next satisfying substitution
     * litExpr will be the calculated value
     * if node is final, there is no next to move to
     *
     * @return false, if no more satisfying assignments can be found FOR DECL
     */
    boolean moveNext() { // gives false, if no more satisfying assignments can be found FOR DECL
        /*if (mapCursor.equals(node.nextExpression.cursor())) {
            // push at the beginning of node examination
            solver.push();
        }*/
        if (mapCursor != null && mapCursor.moveNext()) {
            // there are remaining unexploited results in map
            newNode = mapCursor.value();
            literal = mapCursor.key();
            return true;
        }
        // calculate new result
        mapCursor = null;
        if (node.isFinal) {
            // node revisited, no more solutions
            //solver.pop();
            return false;
        }
        // get next solution from solver,
        // it also sets newNode and literal
        boolean result = getSolverResult();
        if (!result) {
            //solver.pop();
            node.isFinal = true;
        }
        return result;
    }

    boolean getSolverResult() {
        SolverStatus status = solver.check();
        if (status.isUnsat()) {
            // no more satisfying assignments
            node.isFinal = true;
            return false;
        }
        Valuation model = solver.getModel();
        /// save model as queue
        modelMap = model.toMap();
        boolean result = saveSolverLiteral();
        return  result;
    }

    private boolean saveSolverLiteral() {
        Decl decl = node.variableSubstitution.getDecl();
        literal = modelMap.get(decl);
        if (literal == null) {
            // The solver said that the value of decl does not count
            // We choose it false, but later it will be checked whether true is ok.
            literal = FalseExpr.getInstance();
            // literal = DefaultLitExpr.getInstance();
        }
        // not inspected assignment found, create new node accordingly
        // TODO: substituteAll for caching solution
        newNode = node.substitute(literal);
        if (newNode == null || newNode.expression.equals(FalseExpr.getInstance())) return false;
        if (newNode.expression.equals(TrueExpr.getInstance())) return true;
        return true;
    }


/*
    private void doBeforeNewResult() {
        for (Decl d : node.variableSubstitution.decls) {
            if (d.equals(decl)) break;
            try {
                ExpressionNode.solver.add(Eq(d.getRef(), solutionMap.get(d)));
            } catch (Exception e) {
                System.out.println("hiba");
            }
        }
    }

    private void putInMap() {
        if (solutionMap.get(decl) != mapCursor.key())
            changed = true;
        solutionMap.put(decl, mapCursor.key());
        lastPutInMap = decl;
    }
*/

    /**
     * Search for next satisfying substitution
     * litExpr will be the calculated value
     * if node is final, there is no next to move to
     *
     * @return false, if no more satisfying assignments can be found
     */
    /*boolean moveNext() { // gives false, if no more satisfying assignments can be found
        if (node.expression.equals(TrueExpr.getInstance())) {
            resultDecl = decl;
            return true;
        }
        if (decl == null){
            if (changed) {
                resultDecl = decl;
                return true; // itt mar nem kell vizsgalodni
            }
            else return false; // mar voltunk itt, noha itt mar nem kene vizsgalodni
        }
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
            if (nextNode.nodeCursor.decl == null)  // nem kell tovabb szamolni, nem true expression de valami kielegitheto
                return true;
            assert (0==1); // ide nem kéne jutni
            return false;
        }
        // uj megoldas
        //doBeforeNewResult();
        //ExpressionNode.solver.push();
        boolean result = getNewResult();
        //ExpressionNode.solver.pop();
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
    }*/

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