package org.cmdbuild.logic.data.access.lock;

import org.cmdbuild.logic.data.access.lock.LockableStore.Lock;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class DisposingLockableStore<L extends Lock> extends ForwardingLockableStore<L> {

	public static interface Disposer<L extends Lock> {

		void dispose(Lockable lockable, LockableStore<L> store);

	}

	public static class PredicateBasedDisposer<L extends Lock> implements Disposer<L> {

		private final Predicate<L> predicate;

		public PredicateBasedDisposer(final Predicate<L> predicate) {
			this.predicate = predicate;
		}

		@Override
		public void dispose(final Lockable lockable, final LockableStore<L> store) {
			final Optional<L> stored = store.get(lockable);
			if (stored.isPresent()) {
				if (predicate.apply(stored.get())) {
					store.remove(lockable);
				}
			}
		}

	}

	private final LockableStore<L> delegate;
	private final Disposer<L> disposer;

	public DisposingLockableStore(final LockableStore<L> delegate, final Disposer<L> disposer) {
		this.delegate = delegate;
		this.disposer = disposer;
	}

	@Override
	protected LockableStore<L> delegate() {
		return delegate;
	}

	@Override
	public void add(final Lockable lockable, final L lock) {
		disposer.dispose(lockable, delegate());
		delegate().add(lockable, lock);
	}

	@Override
	public void remove(final Lockable lockable) {
		disposer.dispose(lockable, delegate());
		delegate().remove(lockable);
	}

	@Override
	public boolean isPresent(final Lockable lockable) {
		disposer.dispose(lockable, delegate());
		return delegate().isPresent(lockable);
	}

	@Override
	public Optional<L> get(final Lockable lockable) {
		disposer.dispose(lockable, delegate());
		return delegate().get(lockable);
	}

	@Override
	public Iterable<Lockable> stored() {
		for (final Lockable lockable : delegate().stored()) {
			disposer.dispose(lockable, delegate());
		}
		return delegate().stored();
	}

}
