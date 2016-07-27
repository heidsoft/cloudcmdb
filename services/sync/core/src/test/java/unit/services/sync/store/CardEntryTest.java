package unit.services.sync.store;

import static com.google.common.collect.Maps.transformValues;
import static com.google.common.collect.Maps.uniqueIndex;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.common.utils.guava.Functions;
import org.cmdbuild.services.sync.store.Attribute;
import org.cmdbuild.services.sync.store.CardEntry;
import org.cmdbuild.services.sync.store.CardEntry.Builder;
import org.cmdbuild.services.sync.store.ClassType;
import org.cmdbuild.services.sync.store.SimpleAttribute;
import org.junit.Test;

public class CardEntryTest {

	private static final Attribute KEY_ATTRIBUTE_1 = SimpleAttribute.newInstance() //
			.withName("foo") //
			.withKeyStatus(true) //
			.build();
	private static final Attribute KEY_ATTRIBUTE_2 = SimpleAttribute.newInstance() //
			.withName("bar") //
			.withKeyStatus(true) //
			.build();
	private static final Attribute NON_KEY_ATTRIBUTE = SimpleAttribute.newInstance() //
			.withName("baz") //
			.build();
	private static final ClassType TYPE = ClassType.newInstance() //
			.withName("type_name") //
			.withAttribute(KEY_ATTRIBUTE_1) //
			.withAttribute(KEY_ATTRIBUTE_2) //
			.withAttribute(NON_KEY_ATTRIBUTE) //
			.build();

	@Test
	public void entrySuccessfullyCreated() throws Exception {
		// when
		final CardEntry entry = CardEntry.newInstance() //
				.withType(TYPE) //
				.withValue(KEY_ATTRIBUTE_1.getName(), "foo") //
				.withValue(KEY_ATTRIBUTE_2.getName(), "bar") //
				.withValue(NON_KEY_ATTRIBUTE.getName(), "baz") //
				.build();

		// then
		final Map<String, Object> map = asMap(entry.getValues());
		assertThat(map, hasEntry(KEY_ATTRIBUTE_1.getName(), (Object) "foo"));
		assertThat(map, hasEntry(KEY_ATTRIBUTE_2.getName(), (Object) "bar"));
		assertThat(map, hasEntry(NON_KEY_ATTRIBUTE.getName(), (Object) "baz"));
	}

	@Test(expected = NullPointerException.class)
	public void typeIsRequired() throws Exception {
		// given
		final Builder builder = CardEntry.newInstance() //
				.withValue(KEY_ATTRIBUTE_1.getName(), "foo") //
				.withValue(KEY_ATTRIBUTE_2.getName(), "bar");

		// when
		builder.build();
	}

	@Test(expected = NullPointerException.class)
	public void thereMustBeValuesForAllKeyAttributes() throws Exception {
		// given
		final Builder builder = CardEntry.newInstance() //
				.withType(TYPE) //
				.withValue(KEY_ATTRIBUTE_1.getName(), "foo");

		// when
		builder.build();
	}

	@Test(expected = NullPointerException.class)
	public void keyAttributesCannotBeNull() throws Exception {
		// given
		final Builder builder = CardEntry.newInstance() //
				.withType(TYPE) //
				.withValue(KEY_ATTRIBUTE_1.getName(), null) //
				.withValue(KEY_ATTRIBUTE_2.getName(), "bar");

		// when
		builder.build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void keyAttributesCannotBeBlank() throws Exception {
		// given
		final Builder builder = CardEntry.newInstance() //
				.withType(TYPE) //
				.withValue(KEY_ATTRIBUTE_1.getName(), " \t") //
				.withValue(KEY_ATTRIBUTE_2.getName(), "bar");

		// when
		builder.build();
	}

	@Test
	public void nonKeyAttributesAreNotRequired() throws Exception {
		// given
		final Builder builder = CardEntry.newInstance() //
				.withType(TYPE) //
				.withValue(KEY_ATTRIBUTE_1.getName(), "foo") //
				.withValue(KEY_ATTRIBUTE_2.getName(), "bar");

		// when
		builder.build();
	}

	@Test
	public void whenValueIsSpecifiedMoreThanOnceTheLastOneIsStored() throws Exception {
		// given
		final CardEntry entry = CardEntry.newInstance() //
				.withType(TYPE) //
				.withValue(KEY_ATTRIBUTE_1.getName(), "foo") //
				.withValue(KEY_ATTRIBUTE_2.getName(), "bar") //
				.withValue(KEY_ATTRIBUTE_1.getName(), "baz") //
				.build();

		// then
		final Map<String, Object> map = asMap(entry.getValues());
		assertThat(map, hasEntry(KEY_ATTRIBUTE_1.getName(), (Object) "baz"));
		assertThat(map, hasEntry(KEY_ATTRIBUTE_2.getName(), (Object) "bar"));
	}

	@Test
	public void entriesWithDifferentTypeButSameValuesAreDifferent() throws Exception {
		// given
		final ClassType TYPE_1 = ClassType.newInstance() //
				.withName("foo") //
				.withAttribute(KEY_ATTRIBUTE_1) //
				.build();
		final ClassType TYPE_2 = ClassType.newInstance() //
				.withName("bar") //
				.withAttribute(KEY_ATTRIBUTE_1) //
				.build();
		final CardEntry entry1 = CardEntry.newInstance() //
				.withType(TYPE_1) //
				.withValue(KEY_ATTRIBUTE_1.getName(), "foo") //
				.build();
		final CardEntry entry2 = CardEntry.newInstance() //
				.withType(TYPE_2) //
				.withValue(KEY_ATTRIBUTE_1.getName(), "foo") //
				.build();

		// then
		assertThat(entry1, not(equalTo(entry2)));
		assertThat(entry1.hashCode(), not(equalTo(entry2.hashCode())));
	}

	@Test
	public void entriesWithSameTypeAndSameValuesAreEqualAndHaveSameHashCode() throws Exception {
		// given
		final CardEntry entry1 = CardEntry.newInstance() //
				.withType(TYPE) //
				.withValue(KEY_ATTRIBUTE_1.getName(), "foo") //
				.withValue(KEY_ATTRIBUTE_2.getName(), "bar") //
				.build();
		final CardEntry entry2 = CardEntry.newInstance() //
				.withType(TYPE) //
				.withValue(KEY_ATTRIBUTE_1.getName(), "foo") //
				.withValue(KEY_ATTRIBUTE_2.getName(), "bar") //
				.build();
		final CardEntry entry3 = CardEntry.newInstance() //
				.withType(TYPE) //
				.withValue(KEY_ATTRIBUTE_1.getName(), "bar") //
				.withValue(KEY_ATTRIBUTE_2.getName(), "baz") //
				.build();

		// then
		assertThat(entry1, equalTo(entry2));
		assertThat(entry1, not(equalTo(entry3)));
		assertThat(entry1.hashCode(), equalTo(entry2.hashCode()));
		assertThat(entry1.hashCode(), not(equalTo(entry3.hashCode())));
	}

	/*
	 * Utilities
	 */

	private Map<String, Object> asMap(final Iterable<Entry<String, Object>> values) {
		final Map<String, Object> map = transformValues( //
				uniqueIndex( //
						values, //
						Functions.<String, Object> toKey() //
				), //
				Functions.<String, Object> toValue());
		return map;
	}

}
