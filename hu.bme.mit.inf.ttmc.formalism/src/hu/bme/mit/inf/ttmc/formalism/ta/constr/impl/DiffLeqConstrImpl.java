package hu.bme.mit.inf.ttmc.formalism.ta.constr.impl;

import static hu.bme.mit.inf.ttmc.core.expr.impl.Exprs.Int;
import static hu.bme.mit.inf.ttmc.core.expr.impl.Exprs.Leq;
import static hu.bme.mit.inf.ttmc.core.expr.impl.Exprs.Sub;

import hu.bme.mit.inf.ttmc.core.expr.LeqExpr;
import hu.bme.mit.inf.ttmc.formalism.common.expr.ClockRefExpr;
import hu.bme.mit.inf.ttmc.formalism.ta.constr.Clock;
import hu.bme.mit.inf.ttmc.formalism.ta.constr.DiffLeqConstr;
import hu.bme.mit.inf.ttmc.formalism.ta.utils.ConstrVisitor;

final class DiffLeqConstrImpl extends AbstractDiffConstr implements DiffLeqConstr {

	private static final int HASH_SEED = 7019;

	private static final String OPERATOR_LABEL = "<=";

	private volatile LeqExpr expr = null;

	DiffLeqConstrImpl(final Clock leftClock, final Clock rightClock, final int bound) {
		super(leftClock, rightClock, bound);
	}

	@Override
	public LeqExpr asExpr() {
		LeqExpr result = expr;
		if (result == null) {
			final ClockRefExpr leftRef = getLeftClock().asDecl().getRef();
			final ClockRefExpr rightRef = getRightClock().asDecl().getRef();
			result = Leq(Sub(leftRef, rightRef), Int(getBound()));
			expr = result;
		}
		return result;
	}

	@Override
	public <P, R> R accept(final ConstrVisitor<? super P, ? extends R> visitor, final P param) {
		return visitor.visit(this, param);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof DiffLeqConstr) {
			final DiffLeqConstr that = (DiffLeqConstr) obj;
			return this.getBound() == that.getBound() && this.getLeftClock().equals(that.getLeftClock())
					&& this.getRightClock().equals(that.getRightClock());
		} else {
			return false;
		}
	}

	@Override
	protected int getHashSeed() {
		return HASH_SEED;
	}

	@Override
	protected String getOperatorLabel() {
		return OPERATOR_LABEL;
	}

}