package org.cmdbuild.logic.data.access.filter.model;

import static com.google.common.collect.Iterables.elementsEqual;

public class All extends CompositeElement {

	All(final Iterable<Element> elements) {
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
		if (!(obj instanceof All)) {
			return false;
		}
		final All other = All.class.cast(obj);
		return elementsEqual(this.getElements(), other.getElements());
	}

}