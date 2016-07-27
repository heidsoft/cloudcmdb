package org.cmdbuild.services.sync.store.sql;

import org.cmdbuild.services.sync.store.ClassType;

public interface TypeMapping {

	ClassType getType();

	Iterable<AttributeMapping> getAttributeMappings();

}
