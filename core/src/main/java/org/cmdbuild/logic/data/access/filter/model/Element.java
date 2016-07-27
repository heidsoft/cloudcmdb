package org.cmdbuild.logic.data.access.filter.model;

public interface Element {

	void accept(ElementVisitor visitor);

}
