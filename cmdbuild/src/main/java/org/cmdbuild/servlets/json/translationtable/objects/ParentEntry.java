package org.cmdbuild.servlets.json.translationtable.objects;

import java.util.Collection;

import org.codehaus.jackson.annotate.JsonProperty;

public class ParentEntry extends TableEntry {

	private static final String CHILDREN = "children";

	private Collection<TableEntry> children;

	@JsonProperty(CHILDREN)
	public Collection<TableEntry> getChildren() {
		return children;
	}

	public void setChildren(final Collection<TableEntry> children) {
		this.children = children;
	}

}
