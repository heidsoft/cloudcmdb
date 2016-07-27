package org.cmdbuild.model.widget.customform;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;

import org.codehaus.jackson.map.ObjectMapper;

abstract class AttributesBasedModelBuilder implements ModelBuilder {

	protected static final String //
			TYPE_BOOLEAN = "boolean", //
			TYPE_CHAR = "char", //
			TYPE_DATE = "date", //
			TYPE_DATE_TIME = "dateTime", //
			TYPE_DECIMAL = "decimal", //
			TYPE_DOUBLE = "double", //
			TYPE_ENTRY_TYPE = "entryType", //
			TYPE_INTEGER = "integer", //
			TYPE_IP_ADDRESS = "ipAddress", //
			TYPE_LOOKUP = "lookup", //
			TYPE_REFERENCE = "reference", //
			TYPE_STRING_ARRAY = "stringArray", //
			TYPE_STRING = "string", //
			TYPE_TEXT = "text", //
			TYPE_TIME = "time";

	/**
	 * Usable by subclasses only.
	 */
	protected AttributesBasedModelBuilder() {
	}

	@Override
	public final String build() {
		return writeJsonString(newArrayList(attributes()));
	}

	private static String writeJsonString(final Collection<Attribute> attributes) {
		try {
			final ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(attributes);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected abstract Iterable<Attribute> attributes();

}