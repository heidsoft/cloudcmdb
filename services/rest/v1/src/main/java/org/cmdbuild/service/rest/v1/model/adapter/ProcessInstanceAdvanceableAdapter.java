package org.cmdbuild.service.rest.v1.model.adapter;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_ACTIVITY;
import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_ADVANCE;
import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_ID;
import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_NAME;
import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_STATUS;
import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_TYPE;
import static org.cmdbuild.service.rest.v1.model.Models.newProcessInstanceAdvance;
import static org.cmdbuild.service.rest.v1.model.Models.newValues;

import java.util.Map;

import org.cmdbuild.service.rest.v1.model.ProcessInstanceAdvanceable;
import org.cmdbuild.service.rest.v1.model.Values;

import com.google.common.collect.Maps;

public class ProcessInstanceAdvanceableAdapter extends ModelToValuesAdapter<ProcessInstanceAdvanceable> {

	@Override
	protected Values modelToValues(final ProcessInstanceAdvanceable input) {
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
		map.put(UNDERSCORED_ACTIVITY, input.getActivity());
		map.put(UNDERSCORED_ADVANCE, input.isAdvance());
		return newValues() //
				.withValues(map) //
				.build();
	}

	@Override
	protected ProcessInstanceAdvanceable valuesToModel(final Values input) {
		return newProcessInstanceAdvance() //
				.withType(getAndRemove(input, UNDERSCORED_TYPE, String.class)) //
				.withId(getAndRemove(input, UNDERSCORED_ID, Long.class)) //
				.withName(getAndRemove(input, UNDERSCORED_NAME, String.class)) //
				.withStatus(getAndRemove(input, UNDERSCORED_STATUS, Long.class)) //
				.withActivity(getAndRemove(input, UNDERSCORED_ACTIVITY, String.class)) //
				.withAdvance(defaultIfNull(getAndRemove(input, UNDERSCORED_ADVANCE, Boolean.class), false)) //
				.withValues(input) //
				.build();
	}

}
