package org.cmdbuild.services.sync.store.sql;

public interface TableOrViewMapping {

	String getName();

	Iterable<TypeMapping> getTypeMappings();

}
