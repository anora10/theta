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
package hu.bme.mit.theta.analysis.expl;

import static com.google.common.base.Preconditions.checkNotNull;
import static hu.bme.mit.theta.core.type.booltype.BoolExprs.And;

import java.util.*;

import hu.bme.mit.theta.analysis.InitFunc;
import hu.bme.mit.theta.analysis.expr.ExprStates;
import hu.bme.mit.theta.core.decl.ConstDecl;
import hu.bme.mit.theta.core.decl.VarDecl;
import hu.bme.mit.theta.core.model.Valuation;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.booltype.BoolType;
import hu.bme.mit.theta.core.utils.PathUtils;
import hu.bme.mit.theta.core.utils.VarIndexing;
import hu.bme.mit.theta.expressiondiagram.allsat.AllSatSolver;
import hu.bme.mit.theta.expressiondiagram.allsat.AllSatSolverFactory;
import hu.bme.mit.theta.solver.Solver;

public final class ExplInitFunc implements InitFunc<ExplState, ExplPrec> {

	//private final Solver solver;
	AllSatSolverFactory factory;
	private final Expr<BoolType> initExpr;

	private ExplInitFunc(final AllSatSolverFactory factory, final Expr<BoolType> initExpr) {
		this.factory = factory;
		this.initExpr = checkNotNull(initExpr);
	}

	public static ExplInitFunc create(final AllSatSolverFactory factory, final Expr<BoolType> initExpr) {
		return new ExplInitFunc(factory, initExpr);
	}

	@Override
	public Collection<? extends ExplState> getInitStates(final ExplPrec prec) {
		checkNotNull(prec);
//		final Collection<ExplState> initStates0 = ExprStates.createStatesForExpr(initExpr, 0, prec::createState,
//				VarIndexing.all(0));
		final VarIndexing nextIdx = VarIndexing.all(0);
		Collection<ExplState> initStates = new ArrayList<>();
		AllSatSolver allSatSolver = factory.createSolver();
		// create list of followed decls
		List<ConstDecl<?>> decls = new ArrayList<>();
		Set<VarDecl<?>> varDeclSet = prec.getVars();
		for (VarDecl<?> varDecl : varDeclSet) {
			int index = nextIdx.get(varDecl);
			decls.add(varDecl.getConstDecl(index));
		}
		Expr<?> solverExpr = PathUtils.unfold(initExpr, 0);
		allSatSolver.init(solverExpr, decls);

		// We query (max + 1) states from the solver to see if there
		// would be more than max
		while (allSatSolver.hasNext()) {
			Valuation model = allSatSolver.next();
			if (model == null) continue;
			final Valuation valuation = PathUtils.extractValuation(model, nextIdx);
			ExplState explState = prec.createState(valuation);
			initStates.add(explState);
		}

		return initStates.isEmpty() ? Collections.singleton(ExplState.bottom()) : initStates;
	}

}
