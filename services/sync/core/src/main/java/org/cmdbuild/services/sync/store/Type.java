package org.cmdbuild.services.sync.store;

public interface Type {

	void accept(TypeVisitor visitor);

	String getName();

	Iterable<Attribute> getAttributes();

	Attribute getAttribute(String name);

}
