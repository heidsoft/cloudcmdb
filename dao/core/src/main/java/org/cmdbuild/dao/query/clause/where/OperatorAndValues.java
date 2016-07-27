package org.cmdbuild.dao.query.clause.where;

public class OperatorAndValues {

	public static OperatorAndValue beginsWith(final Object value) {
		return BeginsWithOperatorAndValue.beginsWith(value);
	}

	public static OperatorAndValue contains(final Object value) {
		return ContainsOperatorAndValue.contains(value);
	}

	public static OperatorAndValue emptyArray() {
		return EmptyArrayOperatorAndValue.emptyArray();
	}

	public static OperatorAndValue endsWith(final Object value) {
		return EndsWithOperatorAndValue.endsWith(value);
	}

	public static OperatorAndValue eq(final Object value) {
		return EqualsOperatorAndValue.eq(value);
	}

	public static OperatorAndValue gt(final Object value) {
		return GreaterThanOperatorAndValue.gt(value);
	}

	public static OperatorAndValue gteq(final Object value) {
		return new GreaterThanOrEqualToOperatorAndValue(value);
	}

	public static OperatorAndValue in(final Object... objects) {
		return InOperatorAndValue.in(objects);
	}

	public static OperatorAndValue isNull() {
		return NullOperatorAndValue.isNull();
	}

	public static OperatorAndValue lt(final Object value) {
		return LessThanOperatorAndValue.lt(value);
	}

	public static OperatorAndValue lteq(final Object value) {
		return new LessThanOrEqualToOperatorAndValue(value);
	}

	public static OperatorAndValue networkContained(final Object value) {
		return new NetworkContained(value);
	}

	public static OperatorAndValue networkContainedOrEqual(final Object value) {
		return new NetworkContainedOrEqual(value);
	}

	public static OperatorAndValue networkContains(final Object value) {
		return new NetworkContains(value);
	}

	public static OperatorAndValue networkContainsOrEqual(final Object value) {
		return new NetworkContainsOrEqual(value);
	}

	public static OperatorAndValue networkRelationed(final Object value) {
		return new NetworkRelationed(value);
	}

	public static OperatorAndValue stringArrayOverlap(final Object value) {
		return StringArrayOverlapOperatorAndValue.stringArrayOverlap(value);
	}

	private OperatorAndValues() {
		// prevents instantiation
	}

}
