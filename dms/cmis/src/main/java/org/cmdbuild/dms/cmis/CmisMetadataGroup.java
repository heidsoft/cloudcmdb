package org.cmdbuild.dms.cmis;

import java.util.List;

import org.cmdbuild.dms.Metadata;
import org.cmdbuild.dms.MetadataGroup;

public class CmisMetadataGroup implements MetadataGroup {
	private final String name;
	private final List<Metadata> metadataList;

	public CmisMetadataGroup(final String name, final List<Metadata> metadataList) {
		this.name = name;
		this.metadataList = metadataList;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Iterable<Metadata> getMetadata() {
		return metadataList;
	}
}
