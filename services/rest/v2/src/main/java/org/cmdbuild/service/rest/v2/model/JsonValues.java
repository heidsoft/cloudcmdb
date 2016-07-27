package org.cmdbuild.service.rest.v2.model;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.ObjectMapper;

@XmlRootElement
public class JsonValues extends Values {

	private static final ObjectMapper mapper = new ObjectMapper();

	public static JsonValues valueOf(final String value) throws Exception {
		@SuppressWarnings("unchecked")
		final Map<String, Object> values = mapper.readValue(value, Map.class);
		final JsonValues output = new JsonValues();
		output.putAll(values);
		return output;
	}

	JsonValues() {
		// package visibility
	}

}
