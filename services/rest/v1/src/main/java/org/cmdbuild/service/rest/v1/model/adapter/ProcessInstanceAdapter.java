package org.cmdbuild.service.rest.v1.model.adapter;

import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_ID;
import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_NAME;
import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_STATUS;
import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_TYPE;
import static org.cmdbuild.service.rest.v1.model.Models.newProcessInstance;
import static org.cmdbuild.service.rest.v1.model.Models.newValues;

import java.util.Map;

import org.cmdbuild.service.rest.v1.model.ProcessInstance;
import org.cmdbuild.service.rest.v1.model.Values;

import com.google.common.collect.Maps;

public class ProcessInstanceAdapter extends ModelToValuesAdapter<ProcessInstance> {

	@Override
	protected Values modelToValues(final ProcessInstance input) {
		final Map<String, Object> map = Maps.newHashMap();
		map.putAll(input.getValues());
		/*
		 * predefined attributes must always be added at last so they are not
		 * overwritten
		 */
		map.put(UNDERSCORED_TYPE, input.getType());
		map.put(UNDERSCORED_ID, input.getId());
		map.put(UNDERSCORED_NAME, input.getName());
		map.put(UNDERSCORED_STATUS, input.getStatus());
		return newValues() //
				.withValues(map) //
				.build();
	}

	@Override
	protected ProcessInstance valuesToModel(final Values input) {
		return newProcessInstance() //
				.withType(getAndRemove(input, UNDERSCORED_TYPE, String.class)) //
				.withId(getAndRemove(input, UNDERSCORED_ID, Long.class)) //
				.withName(getAndRemove(input, UNDERSCORED_NAME, String.class)) //
				.withValues(input) //
				.build();
	}

}
