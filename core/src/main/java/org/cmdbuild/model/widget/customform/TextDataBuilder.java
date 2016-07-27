package org.cmdbuild.model.widget.customform;

import static com.google.common.base.Splitter.on;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Map;

import org.cmdbuild.model.widget.customform.CustomForm.TextConfiguration;
import org.codehaus.jackson.map.ObjectMapper;

public class TextDataBuilder implements DataBuilder {

	private static final ObjectMapper mapper = new ObjectMapper();

	private final String value;
	private final TextConfiguration textConfiguration;

	public TextDataBuilder(final String value, final TextConfiguration textConfiguration) {
		this.value = value;
		this.textConfiguration = textConfiguration;
	}

	@Override
	public String build() {
		final List<Map<String, String>> maps = newArrayList();
		for (final String element : on(textConfiguration.getRowsSeparator()) //
				.omitEmptyStrings() //
				.trimResults() //
				.split(value)) {
			maps.add(on(textConfiguration.getAttributesSeparator()) //
					.omitEmptyStrings() //
					.trimResults() //
					.withKeyValueSeparator(textConfiguration.getKeyValueSeparator()) //
					.split(element));
		}
		return toJsonString(maps);
	}

	private String toJsonString(final List<Map<String, String>> object) {
		try {
			return mapper.writeValueAsString(object);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

}
