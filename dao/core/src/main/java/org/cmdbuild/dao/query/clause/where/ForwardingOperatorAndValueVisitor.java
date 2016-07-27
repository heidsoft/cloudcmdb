package org.cmdbuild.dao.query.clause.where;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingOperatorAndValueVisitor extends ForwardingObject implements OperatorAndValueVisitor {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingOperatorAndValueVisitor() {
	}

	@Override
	protected abstract OperatorAndValueVisitor delegate();

	@Override
	public void visit(final BeginsWithOperatorAndValue operatorAndValue) {
		delegate().visit(operatorAndValue);
	}

	@Override
	public void visit(final ContainsOperatorAndValue operatorAndValue) {
		delegate().visit(operatorAndValue);
	}

	@Override
	public void visit(final EmptyArrayOperatorAndValue operatorAndValue) {
		delegate().visit(operatorAndValue);
	}

	@Override
	public void visit(final EndsWithOperatorAndValue operatorAndValue) {
		delegate().visit(operatorAndValue);
	}

	@Override
	public void visit(final EqualsOperatorAndValue operatorAndValue) {
		delegate().visit(operatorAndValue);
	}

	@Override
	public void visit(final GreaterThanOperatorAndValue operatorAndValue) {
		delegate().visit(operatorAndValue);
	}

	@Override
	public void visit(final GreaterThanOrEqualToOperatorAndValue operatorAndValue) {
		delegate().visit(operatorAndValue);
	}

	@Override
	public void visit(final InOperatorAndValue operatorAndValue) {
		delegate().visit(operatorAndValue);
	}

	@Override
	public void visit(final LessThanOperatorAndValue operatorAndValue) {
		delegate().visit(operatorAndValue);
	}

	@Override
	public void visit(final LessThanOrEqualToOperatorAndValue operatorAndValue) {
		delegate().visit(operatorAndValue);
	}

	@Override
	public void visit(final NetworkContained operatorAndValue) {
		delegate().visit(operatorAndValue);
	}

	@Override
	public void visit(final NetworkContainedOrEqual operatorAndValue) {
		delegate().visit(operatorAndValue);
	}

	@Override
	public void visit(final NetworkContains operatorAndValue) {
		delegate().visit(operatorAndValue);
	}

	@Override
	public void visit(final NetworkContainsOrEqual operatorAndValue) {
		delegate().visit(operatorAndValue);
	}

	@Override
	public void visit(final NetworkRelationed operatorAndValue) {
		delegate().visit(operatorAndValue);
	}

	@Override
	public void visit(final NullOperatorAndValue operatorAndValue) {
		delegate().visit(operatorAndValue);
	}

	@Override
	public void visit(final StringArrayOverlapOperatorAndValue operatorAndValue) {
		delegate().visit(operatorAndValue);
	}

}
