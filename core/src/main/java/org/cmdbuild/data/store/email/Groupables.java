package org.cmdbuild.data.store.email;

import static org.cmdbuild.data.store.Groupables.nameAndValue;
import static org.cmdbuild.data.store.email.EmailConstants.CARD_ATTRIBUTE;
import static org.cmdbuild.data.store.email.EmailConstants.EMAIL_STATUS_ATTRIBUTE;

import org.cmdbuild.data.store.Groupable;

public class Groupables {

	public static Groupable reference(final Long value) {
		return nameAndValue(CARD_ATTRIBUTE, value);
	}

	public static Groupable status(final Long value) {
		return nameAndValue(EMAIL_STATUS_ATTRIBUTE, value);
	}

	private Groupables() {
		// prevents instantiation
	}

}
