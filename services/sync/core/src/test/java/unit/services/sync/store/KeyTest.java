package unit.services.sync.store;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.cmdbuild.services.sync.store.Attribute;
import org.cmdbuild.services.sync.store.CardEntry;
import org.cmdbuild.services.sync.store.ClassType;
import org.cmdbuild.services.sync.store.SimpleAttribute;
import org.junit.Test;

public class KeyTest {

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
			.withName("foo") //
			.withAttribute(KEY_ATTRIBUTE_1) //
			.withAttribute(KEY_ATTRIBUTE_2) //
			.withAttribute(NON_KEY_ATTRIBUTE) //
			.build();
	private static final ClassType ANOTHER_TYPE = ClassType.newInstance() //
			.withName("bar") //
			.withAttribute(KEY_ATTRIBUTE_1) //
			.withAttribute(KEY_ATTRIBUTE_2) //
			.withAttribute(NON_KEY_ATTRIBUTE) //
			.build();

	@Test
	public void keysOfDifferentTypesAreDifferent() throws Exception {
		// when
		final CardEntry entry1 = CardEntry.newInstance() //
				.withType(TYPE) //
				.withValue(KEY_ATTRIBUTE_1.getName(), "foo") //
				.withValue(KEY_ATTRIBUTE_2.getName(), "bar") //
				.withValue(NON_KEY_ATTRIBUTE.getName(), "baz") //
				.build();
		final CardEntry entry2 = CardEntry.newInstance() //
				.withType(ANOTHER_TYPE) //
				.withValue(KEY_ATTRIBUTE_1.getName(), "foo") //
				.withValue(KEY_ATTRIBUTE_2.getName(), "bar") //
				.withValue(NON_KEY_ATTRIBUTE.getName(), "baz") //
				.build();

		// then
		assertThat(entry1.getKey(), not(equalTo(entry2.getKey())));
	}

	@Test
	public void keysOfSameTypesWithDifferentKeyValuesAreDifferent() throws Exception {
		// when
		final CardEntry entry1 = CardEntry.newInstance() //
				.withType(TYPE) //
				.withValue(KEY_ATTRIBUTE_1.getName(), "foo") //
				.withValue(KEY_ATTRIBUTE_2.getName(), "bar") //
				.withValue(NON_KEY_ATTRIBUTE.getName(), "baz") //
				.build();
		final CardEntry entry2 = CardEntry.newInstance() //
				.withType(TYPE) //
				.withValue(KEY_ATTRIBUTE_1.getName(), "FOO") //
				.withValue(KEY_ATTRIBUTE_2.getName(), "bar") //
				.withValue(NON_KEY_ATTRIBUTE.getName(), "baz") //
				.build();

		// then
		assertThat(entry1.getKey(), not(equalTo(entry2.getKey())));
	}

	@Test
	public void keysOfSameTypesWithSameKeyValuesButDifferentOtherValuesAreEqualAndWithSameHashCode() throws Exception {
		// when
		final CardEntry entry1 = CardEntry.newInstance() //
				.withType(TYPE) //
				.withValue(KEY_ATTRIBUTE_1.getName(), "foo") //
				.withValue(KEY_ATTRIBUTE_2.getName(), "bar") //
				.withValue(NON_KEY_ATTRIBUTE.getName(), "baz") //
				.build();
		final CardEntry entry2 = CardEntry.newInstance() //
				.withType(TYPE) //
				.withValue(KEY_ATTRIBUTE_1.getName(), "foo") //
				.withValue(KEY_ATTRIBUTE_2.getName(), "bar") //
				.withValue(NON_KEY_ATTRIBUTE.getName(), "BAZ") //
				.build();
		final CardEntry entry3 = CardEntry.newInstance() //
				.withType(ANOTHER_TYPE) //
				.withValue(KEY_ATTRIBUTE_1.getName(), "FOO") //
				.withValue(KEY_ATTRIBUTE_2.getName(), "bar") //
				.withValue(NON_KEY_ATTRIBUTE.getName(), "BAZ") //
				.build();

		// then
		assertThat(entry1.getKey(), equalTo(entry2.getKey()));
		assertThat(entry1.getKey().hashCode(), equalTo(entry2.getKey().hashCode()));
		assertThat(entry1.getKey(), not(equalTo(entry3.getKey())));
		assertThat(entry1.getKey().hashCode(), not(equalTo(entry3.getKey().hashCode())));
	}

}
