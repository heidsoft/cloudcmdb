package org.cmdbuild.dao.query.clause.where;

public class Native {

	public static Native of(final String expression) {
		return new Native(expression);
	}

	public final String expression;

	private Native(final String expression) {
		this.expression = expression;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Native)) {
			return false;
		}
		final Native other = Native.class.cast(obj);
		return expression.equals(other.expression);
	}

	@Override
	public int hashCode() {
		return expression.hashCode();
	}

	@Override
	public String toString() {
		return expression;
	}

}
