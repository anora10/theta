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
package hu.bme.mit.theta.analysis.expr;

import static com.google.common.base.Preconditions.checkNotNull;
import static hu.bme.mit.theta.core.type.booltype.BoolExprs.Not;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

import hu.bme.mit.theta.common.logging.ConsoleLogger;
import hu.bme.mit.theta.common.logging.Logger;
import hu.bme.mit.theta.core.model.Valuation;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.LitExpr;
import hu.bme.mit.theta.core.type.booltype.BoolType;
import hu.bme.mit.theta.core.type.booltype.TrueExpr;
import hu.bme.mit.theta.core.utils.PathUtils;
import hu.bme.mit.theta.core.utils.VarIndexing;
import hu.bme.mit.theta.expressiondiagram.allsat.AllSatSolver;
import hu.bme.mit.theta.expressiondiagram.allsat.AllSatSolverFactory;
import hu.bme.mit.theta.expressiondiagram.allsat.BddAllSatSolver;
import hu.bme.mit.theta.expressiondiagram.allsat.BddAllSatSolverFactory;
import hu.bme.mit.theta.solver.Solver;
import hu.bme.mit.theta.solver.utils.WithPushPop;
import hu.bme.mit.theta.solver.z3.Z3SolverFactory;

/**
 * Utility for generating ExprStates.
 */
public final class ExprStates {

	private ExprStates() {
	}

	/**
	 * Generate all states that satisfy a given expression.
	 *
	 * @param factory          AllSAT solver factory
	 * @param expr             Expression to be satisfied
	 * @param exprIndex        Index for unfolding the expression
	 * @param valuationToState Mapping from a valuation to a state
	 * @param stateIndexing    Index for extracting the state
	 * @return States satisfying the expression
	 */
	public static <S extends ExprState> Collection<S> createStatesForExpr(final AllSatSolverFactory factory,
																		  final Expr<BoolType> expr, final int exprIndex,
																		  final Function<? super Valuation, ? extends S> valuationToState, final VarIndexing stateIndexing) {
		return createStatesForExpr(factory, expr, exprIndex, valuationToState, stateIndexing, 0);
	}

	/**
	 * Generate all or a limited number of states that satisfy a given
	 * expression.
	 *
	 * @param factory          AllSAT solver factory
	 * @param expr             Expression to be satisfied
	 * @param exprIndex        Index for unfolding the expression
	 * @param valuationToState Mapping from a valuation to a state
	 * @param stateIndexing    Index for extracting the state
	 * @param limit            Limit the number of states to generate (0 is unlimited)
	 * @return States satisfying the expression
	 */
	public static <S extends ExprState> Collection<S> createStatesForExpr(final AllSatSolverFactory factory,
																		  final Expr<BoolType> expr, final int exprIndex,
																		  final Function<? super Valuation, ? extends S> valuationToState, final VarIndexing stateIndexing,
																		  final int limit) {
//		AllSatSolver allSatSolver = factory.createSolver();
//		Expr solverExpr = PathUtils.unfold(expr, exprIndex);
//		allSatSolver.init(solverExpr);
////		new ConsoleLogger(Logger.Level.MAINSTEP).write(Logger.Level.MAINSTEP, "\n expression: " + solverExpr);
//		final Collection<S> result = new ArrayList<>();
//		while (allSatSolver.hasNext() && (limit == 0 || result.size() < limit)) {
//			final Valuation model = allSatSolver.next();
////			new ConsoleLogger(Logger.Level.MAINSTEP).write(Logger.Level.MAINSTEP, "\n solutionMap: " + model);
////			new ConsoleLogger(Logger.Level.MAINSTEP).write(Logger.Level.MAINSTEP, "\n limit resSize : " + limit + " " + result.size());
//			if (model == null) continue; // no more solutions
////			new ConsoleLogger(Logger.Level.MAINSTEP).write(Logger.Level.MAINSTEP, "\n" + stateIndexing.getMap() + " map found in Varindexing");
//			final Valuation valuation = PathUtils.extractValuation(model, stateIndexing);
////			new ConsoleLogger(Logger.Level.MAINSTEP).write(Logger.Level.MAINSTEP, "\n valuation: " + valuation);
//			final S state = valuationToState.apply(valuation);
////			new ConsoleLogger(Logger.Level.MAINSTEP).write(Logger.Level.MAINSTEP, "\n state: " + state.toExpr());
//			result.add(state);
////			if (PathUtils.unfold(state.toExpr(), stateIndexing).equals(TrueExpr.getInstance())) break;
//		}
//		return  result;

		Solver solver = Z3SolverFactory.getInstance().createSolver();
		try (WithPushPop wpp = new WithPushPop(solver)) {
			Expr solverExpr = PathUtils.unfold(expr, exprIndex);
			solver.add(solverExpr);
		new ConsoleLogger(Logger.Level.MAINSTEP).write(Logger.Level.MAINSTEP, "\n expression: " + solverExpr);
			final Collection<S> result = new ArrayList<>();
			while (solver.check().isSat() && (limit == 0 || result.size() < limit)) {
				new ConsoleLogger(Logger.Level.MAINSTEP).write(Logger.Level.MAINSTEP, "\n solver assertions: " + solver.getAssertions());
				final Valuation model = solver.getModel();
				final Valuation valuation = PathUtils.extractValuation(model, stateIndexing);
				new ConsoleLogger(Logger.Level.MAINSTEP).write(Logger.Level.MAINSTEP, "\n valuation: " + valuation);

				// ez veszi ki a nem kovetetteket
				final S state = valuationToState.apply(valuation);
				result.add(state);
				solver.add(Not(PathUtils.unfold(state.toExpr(), stateIndexing)));
			}
			return result;
		}
	}

}
