package org.cmdbuild.dao.query.clause.where;

public class NetworkContains implements OperatorAndValue {

	private final Object value;

	public NetworkContains(final Object value) {
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
