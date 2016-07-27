package org.cmdbuild.dms.cmis;

import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.commons.definitions.Choice;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.cmdbuild.dms.MetadataDefinition;
import org.cmdbuild.dms.MetadataType;

public class CmisMetadataDefinition implements MetadataDefinition {
	private final String name;
	private final PropertyDefinition<?> propertyDefinition;
	private final MetadataType metadataType;

	public CmisMetadataDefinition(final String name, final PropertyDefinition<?> propertyDefinition,
			final MetadataType metadataType) {
		this.name = name;
		this.propertyDefinition = propertyDefinition;
		this.metadataType = metadataType;
	}

	public PropertyDefinition<?> getProperty() {
		return propertyDefinition;
	}

	@Override
	public String getName() {
		return propertyDefinition.getId();
	}

	@Override
	public String getDescription() {
		return name;
	}

	@Override
	public MetadataType getType() {
		return metadataType;
	}

	@Override
	public boolean isMandatory() {
		return propertyDefinition.isRequired();
	}

	@Override
	public boolean isList() {
		return propertyDefinition.getChoices() != null && !propertyDefinition.getChoices().isEmpty();
	}

	@Override
	public Iterable<String> getListValues() {
		final List<String> values = new ArrayList<String>();
		for (final Choice<?> choice : propertyDefinition.getChoices()) {
			values.add(choice.getDisplayName());
		}
		return values;
	}

	@Override
	public boolean equals(final Object object) {
		if (object == this) {
			return true;
		}
		if (!(object instanceof CmisMetadataDefinition)) {
			return false;
		}
		final CmisMetadataDefinition cmisMetadata = CmisMetadataDefinition.class.cast(object);
		return propertyDefinition.getId().equals(cmisMetadata.propertyDefinition.getId());
	}

	@Override
	public int hashCode() {
		return propertyDefinition.getId().hashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("name", getName()).append("type", getType()).toString();
	}
}
