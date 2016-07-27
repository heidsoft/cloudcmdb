package org.cmdbuild.service.rest.v1.cxf.serialization;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.service.rest.v1.model.Models.newAttachment;
import static org.cmdbuild.service.rest.v1.model.Models.newValues;

import java.util.Date;
import java.util.Map;

import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dms.Metadata;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.service.rest.v1.model.Attachment;
import org.cmdbuild.service.rest.v1.model.Values;

import com.google.common.base.Function;

public class ToAttachment implements Function<StoredDocument, Attachment> {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ToAttachment> {

		private boolean metadata;

		private Builder() {
			// use factory method
		}

		@Override
		public ToAttachment build() {
			validate();
			return new ToAttachment(this);
		}

		private void validate() {
			// TODO Auto-generated method stub
		}

		public Builder withMetadata(final boolean metadata) {
			this.metadata = metadata;
			return this;
		}
	}

	public static Builder newInstance() {
		return new Builder();
	}

	private static final DateAttributeType DATE_ATTRIBUTE_TYPE = new DateAttributeType();

	private static final Iterable<MetadataGroup> NO_METADATA_GROUPS = emptyList();

	private final boolean metadata;

	private ToAttachment(final Builder builder) {
		this.metadata = builder.metadata;
	}

	@Override
	public Attachment apply(final StoredDocument input) {
		return newAttachment() //
				.withId(input.getName()) //
				.withName(input.getName()) //
				.withCategory(input.getCategory()) //
				.withDescription(input.getDescription()) //
				.withVersion(input.getVersion()) //
				.withAuthor(input.getAuthor()) //
				.withCreated(dateAsString(input.getCreated())) //
				.withModified(dateAsString(input.getModified())) //
				.withMetadata(metadata(input.getMetadataGroups())) //
				.build();
	}

	private String dateAsString(final Date input) {
		return DefaultConverter.newInstance() //
				.build() //
				.toClient() //
				.convert(DATE_ATTRIBUTE_TYPE, input) //
				.toString();

	}

	private Values metadata(final Iterable<MetadataGroup> metadataGroups) {
		final Map<String, Object> metadata = newHashMap();
		for (final MetadataGroup element : defaultIfNull(this.metadata ? metadataGroups : null, NO_METADATA_GROUPS)) {
			for (final Metadata subElement : element.getMetadata()) {
				metadata.put(subElement.getName(), subElement.getValue());
			}
		}
		return newValues() //
				.withValues(metadata) //
				.build();
	}

}
