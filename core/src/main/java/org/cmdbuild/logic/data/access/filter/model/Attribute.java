package org.cmdbuild.logic.data.access.filter.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Attribute extends AbstractElement {

	private final String name;
	private final Predicate predicate;

	Attribute(final String name, final Predicate predicate) {
		this.name = name;
		this.predicate = predicate;
	}

	@Override
	public void accept(final ElementVisitor visitor) {
		visitor.visit(this);
	}

	public String getName() {
		return name;
	}

	public Predicate getPredicate() {
		return predicate;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Attribute)) {
			return false;
		}
		final Attribute other = Attribute.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.name, other.name) //
				.append(this.predicate, other.predicate) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(name) //
				.append(predicate) //
				.toHashCode();
	}

}
