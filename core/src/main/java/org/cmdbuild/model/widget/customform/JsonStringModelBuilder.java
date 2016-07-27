package org.cmdbuild.model.widget.customform;

import java.util.Collection;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

class JsonStringModelBuilder extends AttributesBasedModelBuilder {

	private static final Marker MARKER = MarkerFactory.getMarker(JsonStringModelBuilder.class.getName());

	private static final ObjectMapper mapper = new ObjectMapper();

	private static final TypeReference<Collection<? extends Attribute>> TYPE_REFERENCE = new TypeReference<Collection<? extends Attribute>>() {
	};

	private final String expression;

	public JsonStringModelBuilder(final String expression) {
		this.expression = expression;
	}

	@Override
	public Iterable<Attribute> attributes() {
		try {
			logger.debug(MARKER, "parsing expression '{}'", expression);
			return mapper.readValue(expression, TYPE_REFERENCE);
		} catch (final Exception e) {
			logger.error(MARKER, "error parsing expression", e);
			throw new RuntimeException(e);
		}
	}

}