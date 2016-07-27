package unit.services.sync.store;

import static com.google.common.collect.Iterables.size;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.cmdbuild.services.sync.store.Attribute;
import org.cmdbuild.services.sync.store.ClassType;
import org.cmdbuild.services.sync.store.ClassType.Builder;
import org.cmdbuild.services.sync.store.SimpleAttribute;
import org.junit.Test;

public class ClassTypeTest {

	private static final String VALID_NAME = "foo";
	private static final String ANOTHER_VALID_NAME = "bar";
	private static Attribute KEY_ATTRIBUTE = SimpleAttribute.newInstance() //
			.withName("foo") //
			.withKeyStatus(true) //
			.build();
	private static Attribute ANOTHER_KEY_ATTRIBUTE = SimpleAttribute.newInstance() //
			.withName("bar") //
			.withKeyStatus(true) //
			.build();
	private static Attribute NON_KEY_ATTRIBUTE = SimpleAttribute.newInstance() //
			.withName("baz") //
			.build();

	@Test(expected = NullPointerException.class)
	public void nameIsRequired() throws Exception {
		// given
		final Builder builder = ClassType.newInstance() //
				.withAttribute(KEY_ATTRIBUTE);

		// when
		builder.build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void nameMustNotBeBlank() throws Exception {
		// given
		final Builder builder = ClassType.newInstance() //
				.withName(" \t") //
				.withAttribute(KEY_ATTRIBUTE);

		// when
		builder.build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void atLeastOneKeyAttributeIsRequired() throws Exception {
		// given
		final Builder builder = ClassType.newInstance() //
				.withName(VALID_NAME) //
				.withAttribute(NON_KEY_ATTRIBUTE);

		// when
		builder.build();
	}

	@Test
	public void nameAndAtLeastOneKeyAttributeAreTheOnlyRequirements() throws Exception {
		// given
		final Builder builder = ClassType.newInstance() //
				.withName(VALID_NAME) //
				.withAttribute(KEY_ATTRIBUTE);

		// when
		builder.build();
	}

	@Test
	public void multipleAttributesWithSameNameAreLastSettedOneIsConsidered() throws Exception {
		// given
		final Builder builder = ClassType.newInstance() //
				.withName(VALID_NAME) //
				.withAttribute(SimpleAttribute.newInstance() //
						.withName("foo") //
						.build()) //
				.withAttribute(SimpleAttribute.newInstance() //
						.withName("foo") //
						.withKeyStatus(true) //
						.build());

		// when
		final ClassType classType = builder.build();

		// then
		final Iterable<Attribute> attributes = classType.getAttributes();
		assertThat(size(attributes), is(1));
		final Attribute attribute = attributes.iterator().next();
		assertThat(attribute.isKey(), is(true));
	}

	@Test
	public void typesWithSameNameAreEqualAndHaveTheSameHashCode() throws Exception {
		// given
		final ClassType classType1 = ClassType.newInstance() //
				.withName(VALID_NAME) //
				.withAttribute(KEY_ATTRIBUTE) //
				.build();
		final ClassType classType2 = ClassType.newInstance() //
				.withName(VALID_NAME) //
				.withAttribute(ANOTHER_KEY_ATTRIBUTE) //
				.build();
		final ClassType classType3 = ClassType.newInstance() //
				.withName(ANOTHER_VALID_NAME) //
				.withAttribute(KEY_ATTRIBUTE) //
				.build();

		// then
		assertThat(classType1, equalTo(classType2));
		assertThat(classType1, not(equalTo(classType3)));
		assertThat(classType1.hashCode(), equalTo(classType2.hashCode()));
		assertThat(classType1.hashCode(), not(equalTo(classType3.hashCode())));
	}

}
