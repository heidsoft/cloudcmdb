package org.cmdbuild.dao.query.clause.where;

public class LessThanOrEqualToOperatorAndValue implements OperatorAndValue {

	private final Object value;

	LessThanOrEqualToOperatorAndValue(final Object value) {
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
