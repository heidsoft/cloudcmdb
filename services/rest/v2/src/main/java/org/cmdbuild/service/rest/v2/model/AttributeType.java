package org.cmdbuild.service.rest.v2.model;

import static org.cmdbuild.service.rest.v2.constants.Serialization.TYPE_BOOLEAN;
import static org.cmdbuild.service.rest.v2.constants.Serialization.TYPE_CHAR;
import static org.cmdbuild.service.rest.v2.constants.Serialization.TYPE_DATE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.TYPE_DATE_TIME;
import static org.cmdbuild.service.rest.v2.constants.Serialization.TYPE_DECIMAL;
import static org.cmdbuild.service.rest.v2.constants.Serialization.TYPE_DOUBLE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.TYPE_ENTRY_TYPE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.TYPE_FOREIGN_KEY;
import static org.cmdbuild.service.rest.v2.constants.Serialization.TYPE_INTEGER;
import static org.cmdbuild.service.rest.v2.constants.Serialization.TYPE_IP_ADDRESS;
import static org.cmdbuild.service.rest.v2.constants.Serialization.TYPE_LIST;
import static org.cmdbuild.service.rest.v2.constants.Serialization.TYPE_LOOKUP;
import static org.cmdbuild.service.rest.v2.constants.Serialization.TYPE_REFERENCE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.TYPE_STRING;
import static org.cmdbuild.service.rest.v2.constants.Serialization.TYPE_STRING_ARRAY;
import static org.cmdbuild.service.rest.v2.constants.Serialization.TYPE_TEXT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.TYPE_TIME;

public enum AttributeType {

	BOOLEAN(TYPE_BOOLEAN), //
	CHAR(TYPE_CHAR), //
	DATE(TYPE_DATE), //
	DATE_TIME(TYPE_DATE_TIME), //
	DOUBLE(TYPE_DOUBLE), //
	DECIMAL(TYPE_DECIMAL), //
	ENTRY_TYPE(TYPE_ENTRY_TYPE), //
	FOREIGN_KEY(TYPE_FOREIGN_KEY), //
	INTEGER(TYPE_INTEGER), //
	IP_ADDRESS(TYPE_IP_ADDRESS), //
	LIST(TYPE_LIST), //
	LOOKUP(TYPE_LOOKUP), //
	REFERENCE(TYPE_REFERENCE), //
	STRING(TYPE_STRING), //
	STRING_ARRAY(TYPE_STRING_ARRAY), //
	TEXT(TYPE_TEXT), //
	TIME(TYPE_TIME), //
	;

	private final String asString;

	private AttributeType(final String asString) {
		this.asString = asString;
	}

	public String asString() {
		return asString;
	}

}
