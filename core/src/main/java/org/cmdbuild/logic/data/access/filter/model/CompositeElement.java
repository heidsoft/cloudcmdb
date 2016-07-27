package org.cmdbuild.logic.data.access.filter.model;

import static com.google.common.collect.Sets.newHashSet;

import java.util.Iterator;

public abstract class CompositeElement extends AbstractElement implements Iterable<Element> {

	private final Iterable<Element> elements;

	protected CompositeElement(final Iterable<Element> elements) {
		this.elements = newHashSet(elements);
	}

	@Override
	public Iterator<Element> iterator() {
		return elements.iterator();
	}

	public final Iterable<Element> getElements() {
		return elements;
	}

	@Override
	protected final int doHashCode() {
		return getElements().hashCode();
	}

}
