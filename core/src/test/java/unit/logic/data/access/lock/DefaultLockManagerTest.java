package unit.logic.data.access.lock;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.logic.data.access.lock.DefaultLockManager;
import org.cmdbuild.logic.data.access.lock.DefaultLockManager.Lock;
import org.cmdbuild.logic.data.access.lock.DefaultLockManager.Owner;
import org.cmdbuild.logic.data.access.lock.LockManager;
import org.cmdbuild.logic.data.access.lock.LockManager.ExpectedLocked;
import org.cmdbuild.logic.data.access.lock.LockManager.LockedByAnother;
import org.cmdbuild.logic.data.access.lock.Lockable;
import org.cmdbuild.logic.data.access.lock.LockableStore;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

public class DefaultLockManagerTest {

	private static class LockableWithParent implements Lockable {

		private final Lockable parent;

		/**
		 * Use factory method.
		 */
		private LockableWithParent(final Lockable parent) {
			this.parent = parent;
		}

		@Override
		public Optional<Lockable> parent() {
			return of(parent);
		}

	}

	private static final Lockable lockable(final Lockable parent) {
		return new LockableWithParent(parent);
	}

	private static class LockableWithNoParent implements Lockable {

		/**
		 * Use factory method.
		 */
		private LockableWithNoParent() {
		}

		@Override
		public Optional<Lockable> parent() {
			return absent();
		}

	}

	private static final Lockable lockable() {
		return new LockableWithNoParent();
	}

	private static final Owner owner(final String value) {
		return new Owner() {

			@Override
			public String getId() {
				return value;
			}

			@Override
			public String getDescription() {
				return value.toUpperCase();
			}

			@Override
			public boolean equals(final Object obj) {
				if (obj == this) {
					return true;
				}
				if (!(obj instanceof Owner)) {
					return false;
				}
				final Owner other = Owner.class.cast(obj);
				return new EqualsBuilder() //
						.append(this.getId(), other.getId()) //
						.append(this.getDescription(), other.getDescription()) //
						.isEquals();
			}

			@Override
			public int hashCode() {
				return new HashCodeBuilder() //
						.append(getId()) //
						.append(getDescription()) //
						.toHashCode();
			}

			@Override
			public final String toString() {
				return reflectionToString(this, SHORT_PREFIX_STYLE);
			}

		};
	}

	private LockableStore<Lock> store;
	private Supplier<Owner> ownerSupplier;
	private Supplier<Date> timeSupplier;
	private LockManager underTest;

	@Before
	public void setUp() throws Exception {
		store = mock(LockableStore.class);
		ownerSupplier = mock(Supplier.class);
		timeSupplier = new Supplier<Date>() {

			/*
			 * the same for each test
			 */
			private final Date value = new Date();

			@Override
			public Date get() {
				return value;
			}

		};
		underTest = new DefaultLockManager(store, ownerSupplier, timeSupplier);
	}

	@Test
	public void lockWhenUnlocked() throws Exception {
		// given
		doReturn(absent()) //
				.when(store).get(any(Lockable.class));
		doReturn(owner("test")) //
				.when(ownerSupplier).get();
		final Lockable lockable = lockable();

		// when
		underTest.lock(lockable);

		// then
		verify(store).get(eq(lockable));
		verify(ownerSupplier).get();
		verify(store).add(eq(lockable),
				eq(Lock.newInstance() //
						.withOwner(owner("test")) //
						.withTime(timeSupplier.get()) //
						.build()));
		verifyNoMoreInteractions(store, ownerSupplier);
	}

	@Test
	public void lockWhenLockedBySame() throws Exception {
		// given
		doReturn(of(Lock.newInstance() //
				.withOwner(owner("test")) //
				.withTime(timeSupplier.get()) //
				.build())) //
						.when(store).get(any(Lockable.class));
		doReturn(owner("test")) //
				.when(ownerSupplier).get();
		final Lockable lockable = lockable();

		// when
		underTest.lock(lockable);

		// then
		verify(store).get(eq(lockable));
		verify(ownerSupplier, times(2)).get();
		verify(store).add(eq(lockable),
				eq(Lock.newInstance() //
						.withOwner(owner("test")) //
						.withTime(timeSupplier.get()) //
						.build()));
		verifyNoMoreInteractions(store, ownerSupplier);
	}

	@Test(expected = LockedByAnother.class)
	public void lockWhenLockedByOther() throws Exception {
		// given
		doReturn(of(Lock.newInstance() //
				.withOwner(owner("bar")) //
				.withTime(timeSupplier.get()) //
				.build())) //
						.when(store).get(any(Lockable.class));
		doReturn(owner("foo")) //
				.when(ownerSupplier).get();
		final Lockable lockable = lockable();

		// when
		try {
			underTest.lock(lockable);
		} catch (final LockedByAnother e) {
			// then
			verify(store).get(eq(lockable));
			verify(ownerSupplier).get();
			verifyNoMoreInteractions(store, ownerSupplier);

			assertThat(e.getOwner(), equalTo("BAR"));
			assertThat(e.getTime(), equalTo(timeSupplier.get()));

			throw e;
		}
	}

	@Test
	public void unlockWhenUnlocked() throws Exception {
		// given
		doReturn(absent()) //
				.when(store).get(any(Lockable.class));
		final Lockable lockable = lockable();
		final Lockable foo = lockable();
		final Lockable bar = lockable();
		final Lockable baz = lockable(bar);
		doReturn(asList(foo, bar, baz)) //
				.when(store).stored();

		// when
		underTest.unlock(lockable);

		// then
		verify(store).get(eq(lockable));
		verify(store).stored();
		verifyNoMoreInteractions(store, ownerSupplier);
	}

	@Test
	public void unlockWhenLockedBySame() throws Exception {
		// given
		doReturn(of(Lock.newInstance() //
				.withOwner(owner("test")) //
				.withTime(timeSupplier.get()) //
				.build())) //
						.when(store).get(any(Lockable.class));
		doReturn(owner("test")) //
				.when(ownerSupplier).get();
		final Lockable lockable = lockable();

		// when
		underTest.unlock(lockable);

		// then
		verify(store).get(eq(lockable));
		verify(ownerSupplier).get();
		verify(store).remove(eq(lockable));
		verifyNoMoreInteractions(store, ownerSupplier);
	}

	@Test(expected = LockedByAnother.class)
	public void unlockWhenLockedByOther() throws Exception {
		// given
		doReturn(of(Lock.newInstance() //
				.withOwner(owner("test")) //
				.withTime(timeSupplier.get()) //
				.build())) //
						.when(store).get(any(Lockable.class));
		doReturn(owner("not test")) //
				.when(ownerSupplier).get();
		final Lockable lockable = lockable();

		// when
		try {
			underTest.unlock(lockable);
		} catch (final LockedByAnother e) {
			// then
			verify(store).get(eq(lockable));
			verify(ownerSupplier).get();
			verifyNoMoreInteractions(store, ownerSupplier);

			assertThat(e.getOwner(), equalTo("TEST"));
			assertThat(e.getTime(), equalTo(timeSupplier.get()));

			throw e;
		}
	}

	@Test(expected = LockedByAnother.class)
	public void unlockWhenLockedByParent() throws Exception {
		// given
		doReturn(absent())
				.doReturn(of(Lock.newInstance() //
						.withOwner(owner("test")) //
						.withTime(timeSupplier.get()) //
						.build())) //
				.when(store).get(any(Lockable.class));
		final Lockable lockable = lockable();
		final Lockable foo = lockable();
		final Lockable bar = lockable();
		final Lockable baz = lockable(lockable);
		doReturn(asList(foo, bar, baz)) //
				.when(store).stored();

		// when
		try {
			underTest.unlock(lockable);
		} catch (final LockedByAnother e) {
			// then
			verify(store).get(eq(lockable));
			verify(store).stored();
			verify(store).get(eq(baz));
			verifyNoMoreInteractions(store, ownerSupplier);

			assertThat(e.getOwner(), equalTo("TEST"));
			assertThat(e.getTime(), equalTo(timeSupplier.get()));

			throw e;
		}
	}

	@Test
	public void unlockAll() throws Exception {
		// when
		underTest.unlockAll();

		// then
		verify(store).removeAll();
		verifyNoMoreInteractions(store, ownerSupplier);
	}

	@Test(expected = LockedByAnother.class)
	public void checkNotLockedWhenLocked() throws Exception {
		// given
		doReturn(of(Lock.newInstance() //
				.withOwner(owner("test")) //
				.withTime(timeSupplier.get()) //
				.build())) //
						.when(store).get(any(Lockable.class));
		final Lockable lockable = lockable();

		// when
		try {
			underTest.checkNotLocked(lockable);
		} catch (final LockedByAnother e) {
			// then
			verify(store).get(eq(lockable));
			verifyNoMoreInteractions(store, ownerSupplier);

			assertThat(e.getOwner(), equalTo("TEST"));
			assertThat(e.getTime(), equalTo(timeSupplier.get()));

			throw e;
		}
	}

	@Test(expected = LockedByAnother.class)
	public void checkNotLockedWhenLockedByParent() throws Exception {
		// given
		doReturn(absent())
				.doReturn(of(Lock.newInstance() //
						.withOwner(owner("test")) //
						.withTime(timeSupplier.get()) //
						.build())) //
				.when(store).get(any(Lockable.class));
		final Lockable lockable = lockable();
		final Lockable foo = lockable();
		final Lockable bar = lockable();
		final Lockable baz = lockable(lockable);
		doReturn(asList(foo, bar, baz)) //
				.when(store).stored();

		// when
		try {
			underTest.checkNotLocked(lockable);
		} catch (final LockedByAnother e) {
			// then
			verify(store).get(eq(lockable));
			verify(store).stored();
			verify(store).get(eq(baz));
			verifyNoMoreInteractions(store, ownerSupplier);

			assertThat(e.getOwner(), equalTo("TEST"));
			assertThat(e.getTime(), equalTo(timeSupplier.get()));

			throw e;
		}
	}

	@Test
	public void checkNotLockedWhenNotLocked() throws Exception {
		// given
		doReturn(absent())
				.doReturn(of(Lock.newInstance() //
						.withOwner(owner("test")) //
						.withTime(timeSupplier.get()) //
						.build())) //
				.when(store).get(any(Lockable.class));
		final Lockable lockable = lockable();
		final Lockable foo = lockable();
		final Lockable bar = lockable();
		final Lockable baz = lockable(bar);
		doReturn(asList(foo, bar, baz)) //
				.when(store).stored();

		// when
		underTest.checkNotLocked(lockable);

		// then
		verify(store).get(eq(lockable));
		verify(store).stored();
		verifyNoMoreInteractions(store, ownerSupplier);
	}

	@Test(expected = ExpectedLocked.class)
	public void checkLockedByCurrentWhenNotLocked() throws Exception {
		// given
		doReturn(absent()) //
				.when(store).get(any(Lockable.class));
		final Lockable lockable = lockable();

		// when
		try {
			underTest.checkLockedByCurrent(lockable);
		} finally {
			// then
			verify(store).get(eq(lockable));
			verifyNoMoreInteractions(store, ownerSupplier);
		}
	}

	@Test(expected = LockedByAnother.class)
	public void checkLockedByCurrentWhenLockedByOther() throws Exception {
		// given
		doReturn(of(Lock.newInstance() //
				.withOwner(owner("test")) //
				.withTime(timeSupplier.get()) //
				.build())) //
						.when(store).get(any(Lockable.class));
		doReturn(owner("not test")) //
				.when(ownerSupplier).get();
		final Lockable lockable = lockable();

		// when
		try {
			underTest.checkLockedByCurrent(lockable);
		} catch (final LockedByAnother e) {
			// then
			verify(store).get(eq(lockable));
			verify(ownerSupplier).get();
			verifyNoMoreInteractions(store, ownerSupplier);

			assertThat(e.getOwner(), equalTo("TEST"));
			assertThat(e.getTime(), equalTo(timeSupplier.get()));

			throw e;
		}
	}

	@Test
	public void checkLockedByCurrentWhenLockedBySame() throws Exception {
		// given
		doReturn(of(Lock.newInstance() //
				.withOwner(owner("test")) //
				.withTime(timeSupplier.get()) //
				.build())) //
						.when(store).get(any(Lockable.class));
		doReturn(owner("test")) //
				.when(ownerSupplier).get();
		final Lockable lockable = lockable();

		// when
		underTest.checkLockedByCurrent(lockable);

		// then
		verify(store).get(eq(lockable));
		verify(ownerSupplier).get();
		verifyNoMoreInteractions(store, ownerSupplier);
	}

}
