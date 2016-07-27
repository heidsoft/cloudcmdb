package org.cmdbuild.data.store.email;

import static com.google.common.base.Optional.absent;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.contains;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.data.store.email.Predicates.isDefault;

import org.cmdbuild.data.store.ForwardingStore;
import org.cmdbuild.data.store.Store;

import com.google.common.base.Optional;

public class StoreBasedEmailAccountFacade extends ForwardingStore<EmailAccount> implements EmailAccountFacade {

	private static final Iterable<String> NO_NAMES = emptyList();
	private static final Optional<EmailAccount> ABSENT = absent();

	private final Store<EmailAccount> store;

	public StoreBasedEmailAccountFacade(final Store<EmailAccount> store) {
		this.store = store;
	}

	@Override
	protected Store<EmailAccount> delegate() {
		return store;
	}

	@Override
	public Optional<EmailAccount> defaultAccount() {
		return from(readAll()) //
				.filter(isDefault()) //
				.first();
	}

	@Override
	public Optional<EmailAccount> firstOf(final Iterable<String> names) {
		return firstOf(names, ABSENT);
	}

	@Override
	public Optional<EmailAccount> firstOfOrDefault(final Iterable<String> names) {
		return firstOf(names, defaultAccount());
	}

	private Optional<EmailAccount> firstOf(final Iterable<String> names, final Optional<EmailAccount> defaultValue) {
		for (final EmailAccount element : readAll()) {
			if (contains(defaultIfNull(names, NO_NAMES), element.getName())) {
				return Optional.of(element);
			}
		}
		return defaultValue;
	}

	@Override
	public Optional<EmailAccount> fromId(final Long id) {
		for (final EmailAccount element : readAll()) {
			if (element.getId().equals(id)) {
				return Optional.of(element);
			}
		}
		return ABSENT;
	}

}
