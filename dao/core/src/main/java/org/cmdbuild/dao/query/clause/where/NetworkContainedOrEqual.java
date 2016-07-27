package org.cmdbuild.dao.query.clause.where;

public class NetworkContainedOrEqual implements OperatorAndValue {

	private final Object value;

	public NetworkContainedOrEqual(final Object value) {
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
