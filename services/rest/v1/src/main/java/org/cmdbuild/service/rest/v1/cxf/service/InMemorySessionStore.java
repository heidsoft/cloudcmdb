package org.cmdbuild.service.rest.v1.cxf.service;

import static com.google.common.base.Optional.fromNullable;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.service.rest.v1.model.Session;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class InMemorySessionStore implements SessionStore {

	public static interface Configuration {

		long timeout();

	}

	private final Cache<String, Session> store;

	public InMemorySessionStore(final Configuration configuration) {
		if (configuration.timeout() > 0) {
			store = CacheBuilder.newBuilder() //
					.expireAfterAccess(configuration.timeout(), MILLISECONDS) //
					.build();
		} else {
			store = CacheBuilder.newBuilder() //
					.build();
		}
	}

	@Override
	public boolean has(final String id) {
		return store.getIfPresent(id) == null;
	}

	@Override
	public Optional<Session> get(final String id) {
		Validate.notNull(id, "invalid id");
		final Session value = store.getIfPresent(id);
		return fromNullable(value);
	}

	@Override
	public void put(final Session value) {
		Validate.notNull(value, "invalid value");
		Validate.notBlank(value.getId(), "invalid id");
		store.put(value.getId(), value);
	}

	@Override
	public void remove(final String id) {
		Validate.notNull(id, "invalid id");
		store.invalidate(id);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof InMemorySessionStore)) {
			return false;
		}
		final InMemorySessionStore other = InMemorySessionStore.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.store, other.store).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(store) //
				.toHashCode();
	}

	@Override
	public final String toString() {
		return reflectionToString(this, SHORT_PREFIX_STYLE).toString();
	}

}
