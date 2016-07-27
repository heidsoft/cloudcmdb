package org.cmdbuild.service.rest.v1.model.adapter;

import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_DESTINATION_DESCRIPTION;
import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_DESTINATION_ID;
import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_DESTINATION_TYPE;
import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_ID;
import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_SOURCE_DESCRIPTION;
import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_SOURCE_ID;
import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_SOURCE_TYPE;
import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_TYPE;
import static org.cmdbuild.service.rest.v1.model.Models.newCard;
import static org.cmdbuild.service.rest.v1.model.Models.newRelation;
import static org.cmdbuild.service.rest.v1.model.Models.newValues;

import java.util.Map;

import org.cmdbuild.service.rest.v1.model.Card;
import org.cmdbuild.service.rest.v1.model.Relation;
import org.cmdbuild.service.rest.v1.model.Values;

import com.google.common.collect.Maps;

public class RelationAdapter extends ModelToValuesAdapter<Relation> {

	@Override
	protected Values modelToValues(final Relation input) {
		final Map<String, Object> map = Maps.newHashMap();
		map.putAll(input.getValues());
		/*
		 * predefined attributes must always be added at last so they are not
		 * overwritten
		 */
		map.put(UNDERSCORED_TYPE, input.getType());
		map.put(UNDERSCORED_ID, input.getId());
		final Card source = input.getSource();
		map.put(UNDERSCORED_SOURCE_ID, source.getId());
		map.put(UNDERSCORED_SOURCE_TYPE, source.getType());
		final Map<String, Object> sourceValues = source.getValues();
		map.put(UNDERSCORED_SOURCE_DESCRIPTION, sourceValues.get(DESCRIPTION_ATTRIBUTE));
		final Card destination = input.getDestination();
		map.put(UNDERSCORED_DESTINATION_ID, destination.getId());
		map.put(UNDERSCORED_DESTINATION_TYPE, destination.getType());
		final Map<String, Object> destinationValues = destination.getValues();
		map.put(UNDERSCORED_DESTINATION_DESCRIPTION, destinationValues.get(DESCRIPTION_ATTRIBUTE));
		return newValues() //
				.withValues(map) //
				.build();
	}

	@Override
	protected Relation valuesToModel(final Values input) {
		return newRelation() //
				.withType(getAndRemove(input, UNDERSCORED_TYPE, String.class)) //
				.withId(getAndRemove(input, UNDERSCORED_ID, Long.class)) //
				.withSource(newCard() //
						.withType(getAndRemove(input, UNDERSCORED_SOURCE_TYPE, String.class)) //
						.withId(getAndRemove(input, UNDERSCORED_SOURCE_ID, Long.class)) //
						.build()) //
				.withDestination(newCard() //
						.withType(getAndRemove(input, UNDERSCORED_DESTINATION_TYPE, String.class)) //
						.withId(getAndRemove(input, UNDERSCORED_DESTINATION_ID, Long.class)) //
						.build()) //
				.withValues(input) //
				.build();
	}

}
