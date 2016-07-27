package org.cmdbuild.dms.cmis;

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.cmdbuild.dms.MetadataDefinition;
import org.cmdbuild.dms.MetadataGroupDefinition;

public class CmisMetadataGroupDefinition implements MetadataGroupDefinition {

	private final String name;
	private final ObjectType secondaryType;
	private final Map<String, MetadataDefinition> metadataDefinitions;

	public CmisMetadataGroupDefinition(final String name, final ObjectType secondaryType,
			final Iterable<CmisMetadataDefinition> metadataDefinitions) {
		this.name = name;
		this.secondaryType = secondaryType;
		this.metadataDefinitions = new HashMap<String, MetadataDefinition>();
		for (final CmisMetadataDefinition metadata : metadataDefinitions) {
			this.metadataDefinitions.put(metadata.getName(), metadata);
		}
	}

	public ObjectType getSecondaryType() {
		return secondaryType;
	}

	@Override
	public String getName() {
		return name;
	}

	public CmisMetadataDefinition getMetadataDefinition(final String name) {
		return (CmisMetadataDefinition) metadataDefinitions.get(name);
	}

	@Override
	public Iterable<MetadataDefinition> getMetadataDefinitions() {
		return metadataDefinitions.values();
	}

	@Override
	public boolean equals(final Object object) {
		if (object == this) {
			return true;
		}
		if (!(object instanceof CmisMetadataGroupDefinition)) {
			return false;
		}
		final CmisMetadataGroupDefinition cmisMetadataGroup = CmisMetadataGroupDefinition.class.cast(object);
		return name.equals(cmisMetadataGroup.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("name", getName()).toString();
	}
}