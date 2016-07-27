package org.cmdbuild.dms.cmis;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.MetadataGroupDefinition;

public class CmisDocumentType implements DocumentTypeDefinition {
	private final String name;
	private final Map<String, MetadataGroupDefinition> metadataGroupDefinitions;

	public CmisDocumentType(final String name, final Iterable<CmisMetadataGroupDefinition> metadataGroupDefinitions) {
		this.name = name;
		this.metadataGroupDefinitions = new HashMap<String, MetadataGroupDefinition>();
		for (final CmisMetadataGroupDefinition group : metadataGroupDefinitions) {
			this.metadataGroupDefinitions.put(group.getName(), group);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	public CmisMetadataGroupDefinition getMetadataGroupDefinition(final String name) {
		return (CmisMetadataGroupDefinition) metadataGroupDefinitions.get(name);
	}

	@Override
	public Iterable<MetadataGroupDefinition> getMetadataGroupDefinitions() {
		return metadataGroupDefinitions.values();
	}

	@Override
	public boolean equals(final Object object) {
		if (object == this) {
			return true;
		}
		if (!(object instanceof CmisDocumentType)) {
			return false;
		}
		final CmisDocumentType cmisDocumentType = CmisDocumentType.class.cast(object);
		return name.equals(cmisDocumentType.name);
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
