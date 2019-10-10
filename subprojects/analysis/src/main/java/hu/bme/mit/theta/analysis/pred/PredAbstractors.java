/*
 *  Copyright 2017 Budapest University of Technology and Economics
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package hu.bme.mit.theta.analysis.pred;

import static com.google.common.base.Preconditions.checkNotNull;
import static hu.bme.mit.theta.core.type.booltype.BoolExprs.And;
import static hu.bme.mit.theta.core.type.booltype.BoolExprs.Iff;
import static hu.bme.mit.theta.core.type.booltype.BoolExprs.Not;
import static hu.bme.mit.theta.core.type.booltype.BoolExprs.Or;
import static hu.bme.mit.theta.core.type.booltype.BoolExprs.True;

import java.util.*;
import java.util.stream.Collectors;

import hu.bme.mit.theta.core.decl.ConstDecl;
import hu.bme.mit.theta.core.decl.Decl;
import hu.bme.mit.theta.core.decl.Decls;
import hu.bme.mit.theta.core.model.Valuation;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.LitExpr;
import hu.bme.mit.theta.core.type.booltype.*;
import hu.bme.mit.theta.core.utils.PathUtils;
import hu.bme.mit.theta.core.utils.VarIndexing;
import hu.bme.mit.theta.expressiondiagram.ExpressionNode;
import hu.bme.mit.theta.expressiondiagram.ValuationIterator;
import hu.bme.mit.theta.expressiondiagram.VariableSubstitution;
import hu.bme.mit.theta.solver.Solver;
import hu.bme.mit.theta.solver.utils.WithPushPop;

/**
 * Strategies for performing predicate abstraction over an expression.
 */
public class PredAbstractors {

	/**
	 * Interface for performing predicate abstraction over an expression.
	 */
	public interface PredAbstractor {
		/**
		 * Create predicate states for a given expression with a given
		 * precision.
		 *
		 * @param expr         Expression to be abstracted
		 * @param exprIndexing Unfold indexing of the expression
		 * @param prec         Precision
		 * @param precIndexing Unfold indexing of the precision
		 * @return
		 */
		Collection<PredState> createStatesForExpr(final Expr<BoolType> expr, final VarIndexing exprIndexing,
												  final PredPrec prec, final VarIndexing precIndexing);
	}

	/**
	 * Get the strategy that uses Boolean abstraction and splits the disjuncts.
	 *
	 * @param solver
	 * @return
	 */
	public static PredAbstractor booleanSplitAbstractor(final Solver solver) {
		return new BooleanAbstractor(solver, true);
	}

	/**
	 * Get the strategy that uses Boolean BDD abstraction.
	 *
	 * @param solver
	 * @return
	 */
	public static PredAbstractor booleanBddAbstractor(final Solver solver) {
		return new BooleanBddAbstractor(solver, false);
	}

	/**
	 * Get the strategy that uses Boolean abstraction (and keeps the formula as
	 * a whole).
	 *
	 * @param solver
	 * @return
	 */
	public static PredAbstractor booleanAbstractor(final Solver solver) {
		return new BooleanAbstractor(solver, false);
	}

	/**
	 * Get the strategy that uses Cartesian abstraction.
	 *
	 * @param solver
	 * @return
	 */
	public static PredAbstractor cartesianAbstractor(final Solver solver) {
		return new CartesianAbstractor(solver);
	}

	private static final class BooleanBddAbstractor implements PredAbstractor {

		private final Solver solver;
		private final List<ConstDecl<BoolType>> actLits;
		private final String litPrefix;
		private static int instanceCounter = 0;
		private final boolean split;

		public BooleanBddAbstractor(final Solver solver, final boolean split) {
			this.solver = checkNotNull(solver);
			this.actLits = new ArrayList<>();
			this.litPrefix = "__" + getClass().getSimpleName() + "_" + instanceCounter + "_";
			instanceCounter++;
			this.split = split;
		}

		@Override
		public Collection<PredState> createStatesForExpr(final Expr<BoolType> expr, final VarIndexing exprIndexing,
														 final PredPrec prec, final VarIndexing precIndexing) {
			checkNotNull(expr);
			checkNotNull(exprIndexing);
			checkNotNull(prec);
			checkNotNull(precIndexing);

			final List<Expr<BoolType>> preds = new ArrayList<>(prec.getPreds());
			generateActivationLiterals(preds.size());

			assert actLits.size() >= preds.size();

			final List<PredState> states = new LinkedList<>();

			////////////////////////////////////////////////////////////
			/**ez itt mi?**/
			//try (WithPushPop wp = new WithPushPop(solver)) {

				/// Ami itt a solver.add-on belül van, azt kell összeÉSelni
				Expr nodeExpr = PathUtils.unfold(expr, exprIndexing);

				for (int i = 0; i < preds.size(); ++i) {
					nodeExpr = And(Iff(actLits.get(i).getRef(), PathUtils.unfold(preds.get(i), precIndexing)), nodeExpr);

				}
				/// Ezen a ponton van egy kifejezésed, amiben van mindenféle változó, de ebből
				/// az actList-beli változók értékei kellenek majd

				VariableSubstitution vs = ExpressionNode.createDecls(actLits);
				ExpressionNode node = new ExpressionNode(vs);
				node.setExpression(nodeExpr);
				node.calculateSatisfyingSubstitutions();

				// Itt legenerálni az összes megoldást (actLits-re)
				/**ugye actLits.size() a kiszámolandó változók száma?**/
				ValuationIterator valuationIterator = new ValuationIterator(node, actLits.size());
				while (valuationIterator.hasNext()) {
					final Set<Expr<BoolType>> newStatePreds = new HashSet<>();
					Stack<ValuationIterator.Pair> solutionStack = valuationIterator.next();
					if (solutionStack != null) {
						Iterator<ValuationIterator.Pair> it = solutionStack.iterator();
						int predsInd = 0;
						while(it.hasNext()) {
							ValuationIterator.Pair pair = it.next();
							if (pair == null || pair.getValue() == null) {
								//if (pair != null) predsInd++;
								continue;
							}
							final Expr<BoolType> pred = preds.get(predsInd++);
							// pair.getValue() is the value of actLits[predsInd]
							if (pair.getValue().equals(TrueExpr.getInstance())) { // Ha true
								newStatePreds.add(pred);
							}
							else if (pair.getValue().equals(FalseExpr.getInstance())) { // Ha false
								newStatePreds.add(prec.negate(pred));
							}
						}
					}
					if(!newStatePreds.isEmpty() || preds.isEmpty())states.add(PredState.of(newStatePreds));
				}

			//}
			/////////////////////////////////////////////////
			if (!split && states.size() > 1) {
				final Expr<BoolType> pred = Or(states.stream().map(PredState::toExpr).collect(Collectors.toList()));
				return Collections.singleton(PredState.of(pred));
			} else {
				return states;
			}
		}

		private void generateActivationLiterals(final int n) {
			while (actLits.size() < n) {
				actLits.add(Decls.Const(litPrefix + actLits.size(), BoolExprs.Bool()));
			}
		}
	}

	private static final class BooleanAbstractor implements PredAbstractor {

		private final Solver solver;
		private final List<ConstDecl<BoolType>> actLits;
		private final String litPrefix;
		private static int instanceCounter = 0;
		private final boolean split;

		public BooleanAbstractor(final Solver solver, final boolean split) {
			this.solver = checkNotNull(solver);
			this.actLits = new ArrayList<>();
			this.litPrefix = "__" + getClass().getSimpleName() + "_" + instanceCounter + "_";
			instanceCounter++;
			this.split = split;
		}

		@Override
		public Collection<PredState> createStatesForExpr(final Expr<BoolType> expr, final VarIndexing exprIndexing,
														 final PredPrec prec, final VarIndexing precIndexing) {
			checkNotNull(expr);
			checkNotNull(exprIndexing);
			checkNotNull(prec);
			checkNotNull(precIndexing);

			final List<Expr<BoolType>> preds = new ArrayList<>(prec.getPreds());
			generateActivationLiterals(preds.size());

			assert actLits.size() >= preds.size();

			final List<PredState> states = new LinkedList<>();
			try (WithPushPop wp = new WithPushPop(solver)) {
				solver.add(PathUtils.unfold(expr, exprIndexing));
				for (int i = 0; i < preds.size(); ++i) {
					solver.add(Iff(actLits.get(i).getRef(), PathUtils.unfold(preds.get(i), precIndexing)));

				}
				while (solver.check().isSat()) {
					final Valuation model = solver.getModel();
					final Set<Expr<BoolType>> newStatePreds = new HashSet<>();
					final List<Expr<BoolType>> feedback = new LinkedList<>();
					feedback.add(True());
					for (int i = 0; i < preds.size(); ++i) {
						final ConstDecl<BoolType> lit = actLits.get(i);
						final Expr<BoolType> pred = preds.get(i);
						final Optional<LitExpr<BoolType>> eval = model.eval(lit);
						if (eval.isPresent()) {
							if (eval.get().equals(True())) {
								newStatePreds.add(pred);
								feedback.add(lit.getRef());
							} else {
								newStatePreds.add(prec.negate(pred));
								feedback.add(Not(lit.getRef()));
							}
						}
					}
					states.add(PredState.of(newStatePreds));
					solver.add(Not(And(feedback)));
				}
			}
			if (!split && states.size() > 1) {
				final Expr<BoolType> pred = Or(states.stream().map(PredState::toExpr).collect(Collectors.toList()));
				return Collections.singleton(PredState.of(pred));
			} else {
				return states;
			}
		}

		private void generateActivationLiterals(final int n) {
			while (actLits.size() < n) {
				actLits.add(Decls.Const(litPrefix + actLits.size(), BoolExprs.Bool()));
			}
		}
	}

	private static final class CartesianAbstractor implements PredAbstractor {

		private final Solver solver;

		public CartesianAbstractor(final Solver solver) {
			this.solver = solver;
		}

		@Override
		public Collection<PredState> createStatesForExpr(final Expr<BoolType> expr, final VarIndexing exprIndexing,
														 final PredPrec prec, final VarIndexing precIndexing) {
			final List<Expr<BoolType>> newStatePreds = new ArrayList<>();

			try (WithPushPop wp = new WithPushPop(solver)) {
				solver.add(PathUtils.unfold(expr, exprIndexing));
				solver.check();
				if (solver.getStatus().isUnsat()) {
					return Collections.emptySet();
				}

				for (final Expr<BoolType> pred : prec.getPreds()) {
					final boolean ponEntailed;
					final boolean negEntailed;
					try (WithPushPop wp1 = new WithPushPop(solver)) {
						solver.add(PathUtils.unfold(prec.negate(pred), precIndexing));
						ponEntailed = solver.check().isUnsat();
					}
					try (WithPushPop wp2 = new WithPushPop(solver)) {
						solver.add(PathUtils.unfold(pred, precIndexing));
						negEntailed = solver.check().isUnsat();
					}

					assert !(ponEntailed && negEntailed) : "Ponated and negated predicates are both entailed.";

					if (ponEntailed) {
						newStatePreds.add(pred);
					}
					if (negEntailed) {
						newStatePreds.add(prec.negate(pred));
					}
				}
			}

			return Collections.singleton(PredState.of(newStatePreds));
		}

	}
}
