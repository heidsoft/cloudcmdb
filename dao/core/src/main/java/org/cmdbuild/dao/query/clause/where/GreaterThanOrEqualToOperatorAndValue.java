package org.cmdbuild.dao.query.clause.where;

public class GreaterThanOrEqualToOperatorAndValue implements OperatorAndValue {

	private final Object value;

	GreaterThanOrEqualToOperatorAndValue(final Object value) {
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public void accept(final OperatorAndValueVisitor visitor) {
		visitor.visit(this);
	}

}
