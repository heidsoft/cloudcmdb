package org.cmdbuild.services.localization;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.ForwardingCard;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.translation.TranslationFacade;

public class LocalizedCard extends ForwardingCard {

	private final CMCard delegate;

	protected LocalizedCard(final CMCard delegate, final TranslationFacade facade, final LookupStore lookupStore) {
		this.delegate = delegate;
	}

	@Override
	protected CMCard delegate() {
		return delegate;
	}

}
