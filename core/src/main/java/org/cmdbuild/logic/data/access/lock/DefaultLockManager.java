package org.cmdbuild.logic.data.access.lock;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

public class DefaultLockManager implements LockManager {

	public static interface Owner {

		String getId();

		String getDescription();

	}

	public static class Lock implements LockableStore.Lock {

		public static class Builder implements org.apache.commons.lang3.builder.Builder<Lock> {

			private Owner owner;
			private Date time;

			/**
			 * Use factory method.
			 */
			private Builder() {
			}

			@Override
			public Lock build() {
				return new Lock(this);
			}

			public Builder withOwner(final Owner owner) {
				this.owner = owner;
				return this;
			}

			public Builder withTime(final Date time) {
				this.time = time;
				return this;
			}

		}

		public static Builder newInstance() {
			return new Builder();
		}

		private final Owner owner;
		private final Date time;

		private Lock(final Builder builder) {
			this.owner = builder.owner;
			this.time = builder.time;
		}

		public Owner getOwner() {
			return owner;
		}

		public Date getTime() {
			return time;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof Lock)) {
				return false;
			}
			final Lock other = Lock.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.owner, other.owner) //
					.append(this.time, other.time) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(owner) //
					.append(time) //
					.toHashCode();
		}

		@Override
		public final String toString() {
			return reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	public static class DurationExpired implements Predicate<Lock> {

		public static interface Configuration {

			long getExpirationTimeInMilliseconds();

		}

		private final Configuration configuration;

		public DurationExpired(final Configuration configuration) {
			this.configuration = configuration;
		}

		@Override
		public boolean apply(final Lock input) {
			final Date time = input.getTime();
			return (time == null) ? true : expired(time);
		}

		private boolean expired(final Date time) {
			final long now = new Date().getTime();
			return now > (time.getTime() + configuration.getExpirationTimeInMilliseconds());
		}

	}

	public static class OwnerAccepted implements Predicate<Lock> {

		private final Predicate<Owner> delegate;

		public OwnerAccepted(final Predicate<Owner> delegate) {
			this.delegate = delegate;
		}

		@Override
		public boolean apply(final Lock input) {
			return delegate.apply(input.getOwner());
		}

	}

	private static final Supplier<Date> NOW = new Supplier<Date>() {

		@Override
		public Date get() {
			return new Date();
		}

	};

	private final LockableStore<Lock> store;
	private final Supplier<Owner> ownerSupplier;
	private final Supplier<Date> timeSupplier;

	public DefaultLockManager(final LockableStore<Lock> store, final Supplier<Owner> ownerSupplier) {
		this(store, ownerSupplier, NOW);
	}

	/**
	 * Usable by tests.
	 */
	public DefaultLockManager(final LockableStore<Lock> store, final Supplier<Owner> ownerSupplier,
			final Supplier<Date> timeSupplier) {
		this.ownerSupplier = ownerSupplier;
		this.store = store;
		this.timeSupplier = timeSupplier;
	}

	@Override
	public void lock(final Lockable lockable) throws LockedByAnother {
		final Optional<Lock> lock = store.get(lockable);
		if (lock.isPresent() && !sameOwner(lock.get().getOwner(), ownerSupplier.get())) {
			throw lockedByAnother(lock.get());
		}
		store.add(lockable,
				Lock.newInstance() //
						.withOwner(ownerSupplier.get()) //
						.withTime(timeSupplier.get()) //
						.build());
	}

	@Override
	public void unlock(final Lockable lockable) throws LockedByAnother {
		final Optional<Lock> lock = store.get(lockable);
		if (!lock.isPresent()) {
			checkNotLockedAsParent(lockable);
		} else if (!sameOwner(lock.get().getOwner(), ownerSupplier.get())) {
			throw lockedByAnother(lock.get());
		} else {
			store.remove(lockable);
		}
	}

	@Override
	public void unlockAll() {
		store.removeAll();
	}

	@Override
	public void checkNotLocked(final Lockable lockable) throws LockedByAnother {
		final Optional<Lock> lock = store.get(lockable);
		if (lock.isPresent()) {
			throw lockedByAnother(lock.get());
		}
		checkNotLockedAsParent(lockable);
	}

	private void checkNotLockedAsParent(final Lockable lockable) throws LockedByAnother {
		for (final Lockable element : store.stored()) {
			Optional<Lockable> parent = element.parent();
			while (parent.isPresent()) {
				final Lockable _lockable = parent.get();
				if (_lockable.equals(lockable)) {
					final Optional<Lock> _lock = store.get(element);
					throw lockedByAnother(_lock.get());
				}
				parent = _lockable.parent();
			}
		}
	}

	@Override
	public void checkLockedByCurrent(final Lockable lockable) throws ExpectedLocked, LockedByAnother {
		final Optional<Lock> lock = store.get(lockable);
		if (!lock.isPresent()) {
			throw new ExpectedLocked();
		} else if (!sameOwner(lock.get().getOwner(), ownerSupplier.get())) {
			throw lockedByAnother(lock.get());
		}
	}

	private static boolean sameOwner(final Owner o1, final Owner o2) {
		return o1.getId().equals(o2.getId());
	}

	private static LockedByAnother lockedByAnother(final Lock lock) {
		return new LockedByAnother(lock.getOwner().getDescription(), lock.getTime());
	}

}
