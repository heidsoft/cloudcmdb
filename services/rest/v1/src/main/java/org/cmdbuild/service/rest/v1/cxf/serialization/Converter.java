package org.cmdbuild.service.rest.v1.cxf.serialization;

import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;

public interface Converter {

	interface ValueConverter {

		Object convert(CMAttributeType<?> attributeType, Object value);

	}

	ValueConverter toClient();

	ValueConverter fromClient();

}
