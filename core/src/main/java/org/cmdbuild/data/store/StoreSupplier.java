package org.cmdbuild.data.store;

import static com.google.common.collect.FluentIterable.from;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

public class StoreSupplier<T extends Storable> implements Supplier<T> {

	public static <T extends Storable> StoreSupplier<T> of(final Class<T> type, final Store<T> store,
			final Predicate<T> predicate) {
		return new StoreSupplier<T>(type, store, predicate);
	}

	private final Class<T> type;
	private final Store<T> store;
	private final Predicate<T> predicate;

	private StoreSupplier(final Class<T> type, final Store<T> store, final Predicate<T> predicate) {
		this.type = type;
		this.store = store;
		this.predicate = predicate;
	}

	@Override
	public T get() {
		return from(store.readAll()) //
				.filter(type) //
				.filter(predicate) //
				.first() //
				.get();
	}

}
