package org.cmdbuild.dms.cmis;

import org.cmdbuild.dms.Metadata;

public class CmisMetadata implements Metadata {
	private final String name;
	private final String value;

	public CmisMetadata(final String name, final String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getValue() {
		return value;
	}
}
