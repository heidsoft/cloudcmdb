package org.cmdbuild.dao.entrytype;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;

import com.google.common.base.Function;

public class Functions {

	private static class CMClassAllParentsFunction implements Function<CMClass, Iterable<CMClass>> {

		@Override
		public Iterable<CMClass> apply(final CMClass input) {
			final Collection<CMClass> output = newHashSet();
			for (CMClass parent = input.getParent(); parent != null; parent = parent.getParent()) {
				output.add(parent);
			}
			return output;
		}

	}

	private static class CMEntyTypeName implements Function<CMEntryType, String> {

		@Override
		public String apply(final CMEntryType input) {
			return input.getName();
		}

	}

	private static class CMEntyTypeNames implements Function<Iterable<? extends CMEntryType>, Iterable<String>> {

		@Override
		public Iterable<String> apply(final Iterable<? extends CMEntryType> input) {
			return from(input) //
					.transform(name());
		}

	}

	private static class CMEntyTypeAttribute implements Function<String, CMAttribute> {

		private final CMEntryType entryType;

		public CMEntyTypeAttribute(final CMEntryType entryType) {
			this.entryType = entryType;
		}

		@Override
		public CMAttribute apply(final String input) {
			return (entryType == null) ? null : entryType.getAttribute(input);
		}

	}

	private static final CMClassAllParentsFunction ALL_PARENTS = new CMClassAllParentsFunction();
	private static final CMEntyTypeName NAME = new CMEntyTypeName();
	private static final CMEntyTypeNames NAMES = new CMEntyTypeNames();

	public static Function<CMClass, Iterable<CMClass>> allParents() {
		return ALL_PARENTS;
	}

	public static Function<CMEntryType, String> name() {
		return NAME;
	}

	public static Function<Iterable<? extends CMEntryType>, Iterable<String>> names() {
		return NAMES;
	}

	public static Function<String, CMAttribute> attribute(final CMEntryType entryType) {
		return new CMEntyTypeAttribute(entryType);
	}

	private static final Function<CMAttribute, String> ATTRIBUTE_NAME = new Function<CMAttribute, String>() {

		@Override
		public String apply(final CMAttribute input) {
			return input.getName();
		}

	};

	public static Function<CMAttribute, String> attributeName() {
		return ATTRIBUTE_NAME;
	}

	private Functions() {
		// prevents instantiation
	}

}
