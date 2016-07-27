package org.cmdbuild.dao.query.clause.where;

public class NullOperatorAndValueVisitor implements OperatorAndValueVisitor {

	private static final NullOperatorAndValueVisitor INSTANCE = new NullOperatorAndValueVisitor();

	public static NullOperatorAndValueVisitor getInstance() {
		return INSTANCE;
	}

	private NullOperatorAndValueVisitor() {
		// use factory method
	}

	@Override
	public void visit(final BeginsWithOperatorAndValue operatorAndValue) {
		// nothing to do
	}

	@Override
	public void visit(final ContainsOperatorAndValue operatorAndValue) {
		// nothing to do
	}

	@Override
	public void visit(final EmptyArrayOperatorAndValue operatorAndValue) {
		// nothing to do
	}

	@Override
	public void visit(final EndsWithOperatorAndValue operatorAndValue) {
		// nothing to do
	}

	@Override
	public void visit(final EqualsOperatorAndValue operatorAndValue) {
		// nothing to do
	}

	@Override
	public void visit(final GreaterThanOperatorAndValue operatorAndValue) {
		// nothing to do
	}

	@Override
	public void visit(final GreaterThanOrEqualToOperatorAndValue operatorAndValue) {
		// nothing to do
	}

	@Override
	public void visit(final InOperatorAndValue operatorAndValue) {
		// nothing to do
	}

	@Override
	public void visit(final LessThanOperatorAndValue operatorAndValue) {
		// nothing to do
	}

	@Override
	public void visit(final LessThanOrEqualToOperatorAndValue operatorAndValue) {
		// nothing to do
	}

	@Override
	public void visit(final NetworkContained operatorAndValue) {
		// nothing to do
	}

	@Override
	public void visit(final NetworkContainedOrEqual operatorAndValue) {
		// nothing to do
	}

	@Override
	public void visit(final NetworkContains operatorAndValue) {
		// nothing to do
	}

	@Override
	public void visit(final NetworkContainsOrEqual operatorAndValue) {
		// nothing to do
	}

	@Override
	public void visit(final NetworkRelationed operatorAndValue) {
		// nothing to do
	}

	@Override
	public void visit(final NullOperatorAndValue operatorAndValue) {
		// nothing to do
	}

	@Override
	public void visit(final StringArrayOverlapOperatorAndValue operatorAndValue) {
		// nothing to do
	}

}
