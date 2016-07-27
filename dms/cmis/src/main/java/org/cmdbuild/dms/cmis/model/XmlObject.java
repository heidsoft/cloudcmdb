package org.cmdbuild.dms.cmis.model;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public abstract class XmlObject {

	protected XmlObject() {
		// usable by subclasses only
	}

	@Override
	public final String toString() {
		return reflectionToString(this, SHORT_PREFIX_STYLE);
	}

}