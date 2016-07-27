package org.cmdbuild.data.store;

import static com.google.common.base.Suppliers.ofInstance;

import com.google.common.base.Supplier;

public class Storables {

	private static class StorableForSupplier implements Storable {

		private final Supplier<? extends Object> supplier;

		public StorableForSupplier(final Supplier<? extends Object> supplier) {
			this.supplier = supplier;
		}

		@Override
		public String getIdentifier() {
			final Object value = supplier.get();
			final String identifier;
			if (value == null) {
				identifier = null;
			} else if (value instanceof String) {
				identifier = String.class.cast(value);
			} else {
				identifier = value.toString();
			}
			return identifier;
		}
	}

	private static Storable storableOf(final Supplier<? extends Object> supplier) {
		return new StorableForSupplier(supplier);
	}

	public static Storable storableOf(final Long value) {
		return storableOf(ofInstance(value));
	}

	public static Storable storableOf(final Integer value) {
		return storableOf(ofInstance(value));
	}

	public static Storable storableOf(final String value) {
		return storableOf(ofInstance(value));
	}

	private Storables() {
		// prevents instantiation
	}

}
