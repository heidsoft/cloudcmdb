package org.cmdbuild.service.rest.v2.model.adapter;

import static com.google.common.collect.FluentIterable.from;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.service.rest.v2.constants.Serialization.OUTPUT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.UNDERSCORED_ACTIVITY;
import static org.cmdbuild.service.rest.v2.constants.Serialization.UNDERSCORED_ADVANCE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.UNDERSCORED_ID;
import static org.cmdbuild.service.rest.v2.constants.Serialization.UNDERSCORED_NAME;
import static org.cmdbuild.service.rest.v2.constants.Serialization.UNDERSCORED_STATUS;
import static org.cmdbuild.service.rest.v2.constants.Serialization.UNDERSCORED_TYPE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.UNDERSCORED_WIDGETS;
import static org.cmdbuild.service.rest.v2.model.Models.newProcessInstanceAdvance;
import static org.cmdbuild.service.rest.v2.model.Models.newValues;
import static org.cmdbuild.service.rest.v2.model.Models.newWidget;

import java.util.Collection;
import java.util.Map;

import org.cmdbuild.service.rest.v2.model.ProcessInstanceAdvanceable;
import org.cmdbuild.service.rest.v2.model.Values;
import org.cmdbuild.service.rest.v2.model.Widget;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class ProcessInstanceAdvanceableAdapter extends ModelToValuesAdapter<ProcessInstanceAdvanceable> {

	private static final Collection<Object> NO_RAW_WIDGETS = emptyList();

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
				.withWidgets(transform(getAndRemove(input, UNDERSCORED_WIDGETS, Collection.class))) //
				.withValues(input) //
				.build();
	}

	private Collection<Widget> transform(final Collection<Object> rawWidgets) {
		try {
			return from(defaultIfNull(rawWidgets, NO_RAW_WIDGETS)) //
					.transform(new Function<Object, Widget>() {

						@Override
						public Widget apply(final Object input) {
							@SuppressWarnings("unchecked")
							final Map<String, Object> map = Map.class.cast(input);
							return newWidget() //
									.withId(getAndRemove(map, UNDERSCORED_ID, String.class)) //
									.withOutput(getAndRemove(map, OUTPUT, Object.class)) //
									.build();
						}

					}) //
					.toList();
		} catch (final Throwable e) {
			throw new IllegalArgumentException(e);
		}
	}

}
