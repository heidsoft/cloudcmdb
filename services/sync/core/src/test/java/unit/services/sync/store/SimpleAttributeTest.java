package unit.services.sync.store;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.cmdbuild.services.sync.store.SimpleAttribute;
import org.cmdbuild.services.sync.store.SimpleAttribute.Builder;
import org.junit.Test;

public class SimpleAttributeTest {

	@Test(expected = NullPointerException.class)
	public void nameIsRequired() throws Exception {
		// given
		final Builder builder = SimpleAttribute.newInstance();

		// when
		builder.build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void nameMustNotBeBlank() throws Exception {
		// given
		final Builder builder = SimpleAttribute.newInstance() //
				.withName(" \t");

		// when
		builder.build();
	}

	@Test
	public void nameIsTheOnlyRequirement() throws Exception {
		// given
		final Builder builder = SimpleAttribute.newInstance() //
				.withName("foo");

		// when
		builder.build();
	}

	@Test
	public void ifNotSpecifiedAttributeIsNotKey() throws Exception {
		// given
		final Builder builder = SimpleAttribute.newInstance() //
				.withName("foo");

		// when
		final SimpleAttribute attribute = builder.build();

		// then
		assertThat(attribute.getName(), equalTo("foo"));
		assertThat(attribute.isKey(), is(false));
	}

	@Test
	public void keyAttributeSuccessfullyCreated() throws Exception {
		// when
		final SimpleAttribute attribute = SimpleAttribute.newInstance() //
				.withName("foo") //
				.withKeyStatus(true) //
				.build();

		// then
		assertThat(attribute.getName(), equalTo("foo"));
		assertThat(attribute.isKey(), is(true));
	}

	@Test
	public void attributesWithSameNameAreEqualAndHaveSameHashCode() throws Exception {
		// given
		final SimpleAttribute attribute1 = SimpleAttribute.newInstance() //
				.withName("foo") //
				.build();
		final SimpleAttribute attribute2 = SimpleAttribute.newInstance() //
				.withName("foo") //
				.build();
		final SimpleAttribute attribute3 = SimpleAttribute.newInstance() //
				.withName("bar") //
				.build();

		// then
		assertThat(attribute1, equalTo(attribute2));
		assertThat(attribute1, not(equalTo(attribute3)));
		assertThat(attribute1.hashCode(), equalTo(attribute2.hashCode()));
		assertThat(attribute1.hashCode(), not(equalTo(attribute3.hashCode())));
	}

}
