package org.cmdbuild.servlets.json.translationtable.objects;

import org.codehaus.jackson.annotate.JsonProperty;

public class MenuEntry extends ParentEntry {

	private static final String TYPE = "type";

	private String type;

	@JsonProperty(TYPE)
	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

}
