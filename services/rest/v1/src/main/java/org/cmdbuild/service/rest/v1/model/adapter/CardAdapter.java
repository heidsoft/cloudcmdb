package org.cmdbuild.service.rest.v1.model.adapter;

import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_ID;
import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_TYPE;
import static org.cmdbuild.service.rest.v1.model.Models.newCard;
import static org.cmdbuild.service.rest.v1.model.Models.newValues;

import java.util.Map;

import org.cmdbuild.service.rest.v1.model.Card;
import org.cmdbuild.service.rest.v1.model.Values;

import com.google.common.collect.Maps;

public class CardAdapter extends ModelToValuesAdapter<Card> {

	@Override
	protected Values modelToValues(final Card input) {
		final Map<String, Object> map = Maps.newHashMap();
		map.putAll(input.getValues());
		/*
		 * predefined attributes must always be added at last so they are not
		 * overwritten
		 */
		map.put(UNDERSCORED_TYPE, input.getType());
		map.put(UNDERSCORED_ID, input.getId());
		return newValues() //
				.withValues(map) //
				.build();
	}

	@Override
	protected Card valuesToModel(final Values input) {
		return newCard() //
				.withType(getAndRemove(input, UNDERSCORED_TYPE, String.class)) //
				.withId(getAndRemove(input, UNDERSCORED_ID, Long.class)) //
				.withValues(input) //
				.build();
	}

}
