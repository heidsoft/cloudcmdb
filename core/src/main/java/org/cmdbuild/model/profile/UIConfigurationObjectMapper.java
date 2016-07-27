package org.cmdbuild.model.profile;

import static org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

public class UIConfigurationObjectMapper extends ObjectMapper {

	public UIConfigurationObjectMapper() {
		setDeserializationConfig(copyDeserializationConfig() //
				.without(FAIL_ON_UNKNOWN_PROPERTIES) //
		);
		setSerializationConfig(copySerializationConfig() //
				/*
				 * to exclude null values
				 */
				.withSerializationInclusion(Inclusion.NON_NULL) //
				/*
				 * to exclude empty map or array
				 */
				.withSerializationInclusion(Inclusion.NON_EMPTY) //
		);
	}

}
