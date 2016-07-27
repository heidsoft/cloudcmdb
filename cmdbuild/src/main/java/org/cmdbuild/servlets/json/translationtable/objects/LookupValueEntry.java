package org.cmdbuild.servlets.json.translationtable.objects;

import static org.cmdbuild.servlets.json.CommunicationConstants.CODE;
import static org.cmdbuild.servlets.json.CommunicationConstants.TRANSLATION_UUID;

import java.util.Collection;

import org.codehaus.jackson.annotate.JsonProperty;

public class LookupValueEntry {

	private String code;
	private String translationUuid;
	private Collection<EntryField> fields;

	@JsonProperty(CODE)
	public String getDescription() {
		return code;
	}

	@JsonProperty(TRANSLATION_UUID)
	public String getTranslationUuid() {
		return translationUuid;
	}

	@JsonProperty("fields")
	public Collection<EntryField> getFields() {
		return fields;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public void setTranslationUuid(final String translationUuid) {
		this.translationUuid = translationUuid;
	}

	public void setFields(final Collection<EntryField> fields) {
		this.fields = fields;
	}

}
