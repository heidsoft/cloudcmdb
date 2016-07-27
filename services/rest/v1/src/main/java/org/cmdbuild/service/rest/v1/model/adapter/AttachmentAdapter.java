package org.cmdbuild.service.rest.v1.model.adapter;

import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_AUTHOR;
import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_CATEGORY;
import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_CREATED;
import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_DESCRIPTION;
import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_ID;
import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_MODIFIED;
import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_NAME;
import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_VERSION;
import static org.cmdbuild.service.rest.v1.model.Models.newAttachment;
import static org.cmdbuild.service.rest.v1.model.Models.newValues;

import java.util.Map;

import org.cmdbuild.service.rest.v1.model.Attachment;
import org.cmdbuild.service.rest.v1.model.Values;

import com.google.common.collect.Maps;

public class AttachmentAdapter extends ModelToValuesAdapter<Attachment> {

	@Override
	protected Values modelToValues(final Attachment input) {
		final Map<String, Object> map = Maps.newHashMap();
		map.putAll(input.getMetadata());
		/*
		 * predefined attributes must always be added at last so they are not
		 * overwritten
		 */
		map.put(UNDERSCORED_ID, input.getId());
		map.put(UNDERSCORED_NAME, input.getName());
		map.put(UNDERSCORED_CATEGORY, input.getCategory());
		map.put(UNDERSCORED_DESCRIPTION, input.getDescription());
		map.put(UNDERSCORED_VERSION, input.getVersion());
		map.put(UNDERSCORED_AUTHOR, input.getAuthor());
		map.put(UNDERSCORED_CREATED, input.getCreated());
		map.put(UNDERSCORED_MODIFIED, input.getModified());
		return newValues() //
				.withValues(map) //
				.build();
	}

	@Override
	protected Attachment valuesToModel(final Values input) {
		return newAttachment() //
				.withId(getAndRemove(input, UNDERSCORED_ID, String.class)) //
				.withName(getAndRemove(input, UNDERSCORED_NAME, String.class)) //
				.withCategory(getAndRemove(input, UNDERSCORED_CATEGORY, String.class)) //
				.withDescription(getAndRemove(input, UNDERSCORED_DESCRIPTION, String.class)) //
				.withVersion(getAndRemove(input, UNDERSCORED_VERSION, String.class)) //
				.withAuthor(getAndRemove(input, UNDERSCORED_AUTHOR, String.class)) //
				.withCreated(getAndRemove(input, UNDERSCORED_CREATED, String.class)) //
				.withModified(getAndRemove(input, UNDERSCORED_MODIFIED, String.class)) //
				.withMetadata(newValues() //
						.withValues(input) //
						.build()) //
				.build();
	}

}
