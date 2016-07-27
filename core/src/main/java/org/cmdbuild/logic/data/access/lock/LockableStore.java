package org.cmdbuild.logic.data.access.lock;

import com.google.common.base.Optional;

public interface LockableStore<L extends LockableStore.Lock> {

	interface Lock {

	}

	void add(Lockable lockable, L lock);

	void remove(Lockable lockable);

	boolean isPresent(Lockable lockable);

	Optional<L> get(Lockable lockable);

	Iterable<Lockable> stored();

	void removeAll();

}
