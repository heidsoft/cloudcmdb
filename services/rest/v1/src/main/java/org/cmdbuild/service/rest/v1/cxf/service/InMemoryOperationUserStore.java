package org.cmdbuild.service.rest.v1.cxf.service;

import static com.google.common.base.Objects.firstNonNull;
import static com.google.common.collect.Maps.newHashMap;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.service.rest.v1.model.Session;

import com.google.common.base.Optional;

public class InMemoryOperationUserStore implements OperationUserStore {

	private static class BySessionImpl implements BySession {

		private static final Optional<OperationUser> ABSENT = Optional.absent();

		private OperationUser value;
		private OperationUser impersonate;

		@Override
		public void main(final OperationUser value) {
			Validate.notNull(value, "invalid value");
			this.value = value;
		}

		@Override
		public void impersonate(final OperationUser value) {
			Validate.notNull(firstNonNull(this.value, value), "invalid value");
			this.impersonate = value;
		}

		@Override
		public Optional<OperationUser> get() {
			return (impersonate == null) ? (value == null) ? ABSENT : Optional.of(value) : Optional.of(impersonate);
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof BySessionImpl)) {
				return false;
			}
			final BySessionImpl other = BySessionImpl.class.cast(obj);
			return new EqualsBuilder() //
					.append(value, other.value) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(value) //
					.toHashCode();
		}

		@Override
		public final String toString() {
			return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE).toString();
		}

	}

	private final Map<String, BySession> map;

	public InMemoryOperationUserStore() {
		map = newHashMap();
	}

	@Override
	public BySession of(final Session value) {
		Validate.notNull(value, "invalid '%s'", Session.class);
		final String id = value.getId();
		final BySession bySession;
		if (map.containsKey(id)) {
			bySession = map.get(id);
		} else {
			bySession = new BySessionImpl();
			map.put(id, bySession);
		}
		return bySession;
	}

	@Override
	public void remove(final Session value) {
		Validate.notNull(value, "invalid '%s'", Session.class);
		map.remove(value.getId());
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof InMemoryOperationUserStore)) {
			return false;
		}
		final InMemoryOperationUserStore other = InMemoryOperationUserStore.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.map, other.map).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(map) //
				.toHashCode();
	}

	@Override
	public final String toString() {
		return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE).toString();
	}

}
