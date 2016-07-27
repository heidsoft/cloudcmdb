package org.cmdbuild.data.store.email;

import java.util.NoSuchElementException;

import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupImpl;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;

public class LookupStoreEmailStatusConverter implements EmailStatusConverter {

	private final LookupStore store;

	public LookupStoreEmailStatusConverter(final LookupStore store) {
		this.store = store;
	}

	@Override
	public EmailStatus fromId(final Long id) {
		final Lookup lookup = store.read(LookupImpl.newInstance() //
				.withId(id) //
				.build());
		return EmailStatus.of(identifierOf(lookup));
	}

	@Override
	public Long toId(final EmailStatus emailStatus) {
		if (emailStatus == null) {
			return null;
		}
		for (final Lookup lookup : store.readAll(LookupType.newInstance() //
				.withName(EmailStatus.LOOKUP_TYPE) //
				.build())) {
			if (identifierOf(lookup).equals(emailStatus.getLookupName())) {
				return lookup.getId();
			}
		}
		throw new NoSuchElementException();
	}

	private String identifierOf(final Lookup lookup) {
		return lookup.code();
	}

}
