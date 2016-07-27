package org.cmdbuild.model.dashboard;

import static org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

public class DashboardObjectMapper extends ObjectMapper {

	public DashboardObjectMapper() {
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
