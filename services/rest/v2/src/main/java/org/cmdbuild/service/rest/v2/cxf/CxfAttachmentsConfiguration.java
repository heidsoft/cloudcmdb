package org.cmdbuild.service.rest.v2.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static org.cmdbuild.service.rest.v2.model.Models.newAttribute;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;

import java.util.Map;

import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.MetadataDefinition;
import org.cmdbuild.dms.MetadataGroupDefinition;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.service.rest.v2.AttachmentsConfiguration;
import org.cmdbuild.service.rest.v2.cxf.serialization.ToAttachmentCategory;
import org.cmdbuild.service.rest.v2.model.AttachmentCategory;
import org.cmdbuild.service.rest.v2.model.Attribute;
import org.cmdbuild.service.rest.v2.model.AttributeType;
import org.cmdbuild.service.rest.v2.model.Models.AttributeBuilder;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;

public class CxfAttachmentsConfiguration implements AttachmentsConfiguration {

	private static final ToAttachmentCategory TO_ATTACHMENT_CATEGORY = new ToAttachmentCategory();

	private final DmsLogic dmsLogic;

	public CxfAttachmentsConfiguration(final DmsLogic dmsLogic) {
		this.dmsLogic = dmsLogic;
	}

	@Override
	public ResponseMultiple<AttachmentCategory> readCategories() {
		final Iterable<DocumentTypeDefinition> elements = dmsLogic.getConfiguredCategoryDefinitions();
		return newResponseMultiple(AttachmentCategory.class) //
				.withElements(from(elements) //
						.transform(TO_ATTACHMENT_CATEGORY)) //
				.withMetadata(newMetadata() //
						.withTotal(Long.valueOf(size(elements))).build()) //
				.build();
	}

	@Override
	public ResponseMultiple<Attribute> readCategoryAttributes(final String categoryId) {
		final Map<String, Attribute> elements = newLinkedHashMap();
		final DocumentTypeDefinition definition = dmsLogic.getCategoryDefinition(categoryId);
		for (final MetadataGroupDefinition groupDefinition : definition.getMetadataGroupDefinitions()) {
			for (final MetadataDefinition metadataDefinition : groupDefinition.getMetadataDefinitions()) {
				final AttributeBuilder attribute = newAttribute() //
						.withId(metadataDefinition.getName()) //
						.withName(metadataDefinition.getName()) //
						.withDescription(metadataDefinition.getDescription()) //
						.withGroup(groupDefinition.getName()) //
						.thatIsMandatory(metadataDefinition.isMandatory()) //
						.thatIsActive(true) //
						/*
						 * custom attributes are never first
						 */
						.withIndex(Long.valueOf(elements.size()));
				switch (metadataDefinition.getType()) {
				case TEXT:
					attribute.withType(AttributeType.TEXT.asString());
					break;
				case INTEGER:
					attribute.withType(AttributeType.INTEGER.asString());
					break;
				case FLOAT:
					attribute.withType(AttributeType.DOUBLE.asString());
					break;
				case DATE:
					attribute.withType(AttributeType.DATE.asString());
					break;
				case DATETIME:
					attribute.withType(AttributeType.DATE_TIME.asString());
					break;
				case BOOLEAN:
					attribute.withType(AttributeType.BOOLEAN.asString());
					break;
				case LIST:
					attribute.withType(AttributeType.LIST.asString()) //
							.withValues(metadataDefinition.getListValues());
					break;
				default:
					attribute.withType(AttributeType.TEXT.asString());
					break;
				}
				elements.put(metadataDefinition.getName(), attribute.build());
			}
		}
		return newResponseMultiple(Attribute.class) //
				.withElements(elements.values()) //
				.withMetadata(newMetadata() //
						.withTotal(Long.valueOf(elements.size())) //
						.build()) //
				.build();
	}
}
