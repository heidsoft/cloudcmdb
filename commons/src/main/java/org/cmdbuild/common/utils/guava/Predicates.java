package org.cmdbuild.common.utils.guava;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Predicate;

public class Predicates {

	private static enum StringPredicate implements Predicate<String> {

		IS_NOT_BLANK() {

			@Override
			public boolean apply(final String input) {
				return StringUtils.isNotBlank(input);
			}

		}

	}

	public static Predicate<String> isNotBlank() {
		return StringPredicate.IS_NOT_BLANK;
	}

	private Predicates() {
		// prevents instantiation
	}

}