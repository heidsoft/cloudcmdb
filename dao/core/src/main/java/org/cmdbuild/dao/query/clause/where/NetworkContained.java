package org.cmdbuild.dao.query.clause.where;

public class NetworkContained implements OperatorAndValue {

	private final Object value;

	public NetworkContained(final Object value) {
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
