package org.cmdbuild.logic.data.access.filter.model;

import static java.util.Arrays.asList;

public class Elements {

	public static Element all(final Element... elements) {
		return all(asList(elements));
	}

	public static Element all(final Iterable<Element> elements) {
		return new All(elements);
	}

	public static Element attribute(final String name, final Predicate predicate) {
		return new Attribute(name, predicate);
	}

	public static Element oneOf(final Element... elements) {
		return oneOf(asList(elements));
	}

	public static Element oneOf(final Iterable<Element> elements) {
		return new OneOf(elements);
	}

	private Elements() {
		// prevents instantiation
	}

}
