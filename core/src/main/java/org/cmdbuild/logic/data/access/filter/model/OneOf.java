package org.cmdbuild.logic.data.access.filter.model;

public class OneOf extends CompositeElement {

	OneOf(final Iterable<Element> elements) {
		super(elements);
	}

	@Override
	public void accept(final ElementVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof OneOf)) {
			return false;
		}
		final OneOf other = OneOf.class.cast(obj);
		return this.getElements().equals(other.getElements());
	}

}