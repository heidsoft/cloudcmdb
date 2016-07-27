package unit.logic.data.access.filter.model;

import static org.cmdbuild.logic.data.access.filter.model.Elements.all;
import static org.cmdbuild.logic.data.access.filter.model.Elements.attribute;
import static org.cmdbuild.logic.data.access.filter.model.Elements.oneOf;
import static org.cmdbuild.logic.data.access.filter.model.Predicates.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.cmdbuild.logic.data.access.filter.model.Element;
import org.junit.Test;

public class ElementsTest {

	@Test
	public void allTest() throws Exception {
		// given
		final Element base = all(attribute("foo", equalTo(123)), attribute("bar", equalTo(456)));
		final Element duplicateAttribute = all(attribute("foo", equalTo(123)), attribute("foo", equalTo(123)),
				attribute("bar", equalTo(456)));
		final Element attributesDifferentOrder = all(attribute("bar", equalTo(456)), attribute("foo", equalTo(123)));
		final Element attributesDifferentOrderAndDuplicateAttribute = all(attribute("bar", equalTo(456)),
				attribute("foo", equalTo(123)), attribute("bar", equalTo(456)));
		final Element missingAttribute = all(attribute("foo", equalTo(123)));

		// then
		assertThat(base, is(duplicateAttribute));
		assertThat(base.hashCode(), is(duplicateAttribute.hashCode()));

		assertThat(base, is(attributesDifferentOrder));
		assertThat(base.hashCode(), is(attributesDifferentOrder.hashCode()));

		assertThat(base, is(attributesDifferentOrderAndDuplicateAttribute));
		assertThat(base.hashCode(), is(attributesDifferentOrderAndDuplicateAttribute.hashCode()));

		assertThat(base, not(is(missingAttribute)));
		assertThat(base.hashCode(), not(is(missingAttribute.hashCode())));
	}

	@Test
	public void attributeTest() throws Exception {
		// given
		final Element base = attribute("foo", equalTo(123));
		final Element same = attribute("foo", equalTo(123));
		final Element differentPredicate = attribute("foo", equalTo(456));
		final Element differentName = attribute("bar", equalTo(123));
		final Element completelyDifferent = attribute("bar", equalTo("baz"));

		// then
		assertThat(base, is(same));
		assertThat(base.hashCode(), is(same.hashCode()));

		assertThat(base, not(is(differentPredicate)));
		assertThat(base.hashCode(), not(is(differentPredicate.hashCode())));

		assertThat(base, not(is(differentName)));
		assertThat(base.hashCode(), not(is(differentName.hashCode())));

		assertThat(base, not(is(completelyDifferent)));
		assertThat(base.hashCode(), not(is(completelyDifferent.hashCode())));
	}

	@Test
	public void oneOfTest() throws Exception {
		// given
		final Element base = oneOf(attribute("foo", equalTo(123)), attribute("bar", equalTo(456)));
		final Element duplicateAttribute = oneOf(attribute("foo", equalTo(123)), attribute("foo", equalTo(123)),
				attribute("bar", equalTo(456)));
		final Element attributesDifferentOrder = oneOf(attribute("bar", equalTo(456)), attribute("foo", equalTo(123)));
		final Element attributesDifferentOrderAndDuplicateAttribute = oneOf(attribute("bar", equalTo(456)),
				attribute("foo", equalTo(123)), attribute("bar", equalTo(456)));
		final Element missingAttribute = oneOf(attribute("foo", equalTo(123)));

		// then
		assertThat(base, is(duplicateAttribute));
		assertThat(base.hashCode(), is(duplicateAttribute.hashCode()));

		assertThat(base, is(attributesDifferentOrder));
		assertThat(base.hashCode(), is(attributesDifferentOrder.hashCode()));

		assertThat(base, is(attributesDifferentOrderAndDuplicateAttribute));
		assertThat(base.hashCode(), is(attributesDifferentOrderAndDuplicateAttribute.hashCode()));

		assertThat(base, not(is(missingAttribute)));
		assertThat(base.hashCode(), not(is(missingAttribute.hashCode())));
	}

}
