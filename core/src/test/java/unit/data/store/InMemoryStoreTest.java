package unit.data.store;

import static com.google.common.collect.Iterables.size;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.cmdbuild.data.store.Groupables.nameAndValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.cmdbuild.data.store.InMemoryStore;
import org.cmdbuild.data.store.Storable;
import org.junit.Before;
import org.junit.Test;

public class InMemoryStoreTest {

	private static class StorableTestDouble implements Storable {

		public static class Builder implements org.apache.commons.lang3.builder.Builder<StorableTestDouble> {

			private String identifier;
			private String value;

			private Builder() {
				// user factory method
			}

			@Override
			public StorableTestDouble build() {
				return new StorableTestDouble(this);
			}

			public Builder withIdentifier(final String identifier) {
				this.identifier = identifier;
				return this;
			}

			public Builder withValue(final String value) {
				this.value = value;
				return this;
			}

		}

		public static Builder newInstance() {
			return new Builder();
		}

		private final String identifier;
		private final String value;

		private StorableTestDouble(final Builder builder) {
			this.identifier = builder.identifier;
			this.value = builder.value;
		}

		@Override
		public String getIdentifier() {
			return identifier;
		}

		public String getValue() {
			return value;
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(identifier) //
					.append(value) //
					.toHashCode();
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof StorableTestDouble)) {
				return false;
			}
			final StorableTestDouble other = StorableTestDouble.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.identifier, other.identifier) //
					.append(this.value, other.value) //
					.isEquals();
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	private InMemoryStore<StorableTestDouble> store;

	@Before
	public void setUp() throws Exception {
		store = InMemoryStore.of(StorableTestDouble.class);
	}

	@Test
	public void storableInterfaceHasOneMethodOnly() throws Exception {
		assertThat(Storable.class.getMethods().length, equalTo(1));
	}

	@Test
	public void elementCreatedAndRead() throws Exception {
		// given
		final StorableTestDouble toBeCreated = StorableTestDouble.newInstance() //
				.withIdentifier("foo") //
				.build();

		// when
		final Storable stored = store.create(toBeCreated);
		final StorableTestDouble readed = store.read(stored);

		// then
		assertThat(readed, equalTo(StorableTestDouble.newInstance() //
				.withIdentifier("foo") //
				.build()));
	}

	@Test
	public void multipleElementsCreatedAndRead() throws Exception {
		// given
		final StorableTestDouble foo = StorableTestDouble.newInstance() //
				.withIdentifier("foo") //
				.build();
		final StorableTestDouble bar = StorableTestDouble.newInstance() //
				.withIdentifier("bar") //
				.build();
		final StorableTestDouble baz = StorableTestDouble.newInstance() //
				.withIdentifier("baz") //
				.build();

		// when
		store.create(foo);
		store.create(bar);
		store.create(baz);
		final Iterable<StorableTestDouble> elements = store.readAll();

		// then
		assertThat(elements, containsInAnyOrder(foo, bar, baz));
	}

	@Test
	public void elementCreatedUpdatedAndRead() throws Exception {
		// given
		final StorableTestDouble foo = StorableTestDouble.newInstance() //
				.withIdentifier("foo") //
				.build();

		// when
		final Storable stored = store.create(foo);
		store.update(StorableTestDouble.newInstance() //
				.withIdentifier("foo") //
				.withValue("bar") //
				.build());
		final StorableTestDouble readed = store.read(stored);

		// then
		assertThat(readed.getValue(), equalTo("bar"));
	}

	@Test
	public void elementAddedAndDeleted() throws Exception {
		// given
		final StorableTestDouble foo = StorableTestDouble.newInstance() //
				.withIdentifier("foo") //
				.build();

		// when
		final Storable created = store.create(foo);
		store.read(created);
		store.delete(created);
		final Iterable<StorableTestDouble> elements = store.readAll();

		// then
		assertThat(size(elements), equalTo(0));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void elementsAreNotGroupableYet() throws Exception {
		store.readAll(nameAndValue("foo", "bar"));
	}

}
