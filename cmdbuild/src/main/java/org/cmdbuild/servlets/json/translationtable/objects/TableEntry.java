package org.cmdbuild.servlets.json.translationtable.objects;

import static org.cmdbuild.servlets.json.CommunicationConstants.NAME;

import java.util.Collection;

import org.codehaus.jackson.annotate.JsonProperty;

public class TableEntry implements TranslationSerialization {
	private static final String FIELDS = "fields";
	private String name;
	private Collection<EntryField> fields;

	@JsonProperty(NAME)
	public String getName() {
		return name;
	}

	@JsonProperty(FIELDS)
	public Collection<EntryField> getFields() {
		return fields;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setFields(final Collection<EntryField> fields) {
		this.fields = fields;
	}

}
