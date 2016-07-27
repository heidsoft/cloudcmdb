package org.cmdbuild.logic.data.access.filter.model;

import static java.util.Arrays.asList;

public class Predicates {

	private static final IsNull IS_NULL = new IsNull();

	public static Predicate and(final Predicate... predicates) {
		return and(asList(predicates));
	}

	public static Predicate and(final Iterable<Predicate> predicates) {
		return new And(predicates);
	}

	public static Predicate contains(final Object value) {
		return new Contains(value);
	}

	public static Predicate endsWith(final Object value) {
		return new EndsWith(value);
	}

	public static Predicate equalTo(final Object value) {
		return new EqualTo(value);
	}

	public static Predicate greaterThan(final Object value) {
		return new GreaterThan(value);
	}

	public static Predicate in(final Object... values) {
		return in(asList(values));
	}

	public static Predicate in(final Iterable<? extends Object> values) {
		return new In(values);
	}

	public static Predicate isNull() {
		return IS_NULL;
	}

	public static Predicate lessThan(final Object value) {
		return new LessThan(value);
	}

	public static Predicate like(final Object value) {
		return new Like(value);
	}

	public static Predicate not(final Predicate predicate) {
		return new Not(predicate);
	}

	public static Predicate or(final Predicate... predicates) {
		return or(asList(predicates));
	}

	public static Predicate or(final Iterable<Predicate> predicates) {
		return new Or(predicates);
	}

	public static Predicate startsWith(final Object value) {
		return new StartsWith(value);
	}

	private Predicates() {
		// prevents instantiation
	}

}
