package integration.logic.data;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class AttributesMatcher extends TypeSafeMatcher<Iterable<? extends CMAttribute>> {

	private final String name;

	private AttributesMatcher(final String name) {
		this.name = name;
	}

	@Override
	protected boolean matchesSafely(final Iterable<? extends CMAttribute> attributes) {
		for (final CMAttribute attribute : attributes) {
			if (attribute.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void describeTo(final Description description) {
		description.appendText("attributes containing one with name ").appendValue(name);
	}

	public static Matcher<Iterable<? extends CMAttribute>> hasAttributeWithName(final String name) {
		return new AttributesMatcher(name);
	}

}
