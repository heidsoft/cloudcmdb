package org.cmdbuild.services.localization;

import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entry.ForwardingRelation;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.translation.TranslationFacade;

public class LocalizedRelation extends ForwardingRelation {

	private final CMRelation delegate;

	protected LocalizedRelation(final CMRelation delegate, final TranslationFacade facade, final LookupStore lookupStore) {
		this.delegate = delegate;
	}

	@Override
	protected CMRelation delegate() {
		return delegate;
	}

}
