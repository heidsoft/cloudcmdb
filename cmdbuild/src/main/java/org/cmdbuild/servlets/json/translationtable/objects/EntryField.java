package org.cmdbuild.servlets.json.translationtable.objects;

import static org.cmdbuild.servlets.json.CommunicationConstants.NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.TRANSLATIONS;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

public class EntryField {

	private static final String VALUE = "value";
	private String name;
	private String value; // default value
	private Map<String, String> translations; // key = the language, value = the
												// translation

	@JsonProperty(NAME)
	public String getName() {
		return name;
	}

	@JsonProperty(VALUE)
	public String getValue() {
		return value;
	}

	@JsonProperty(TRANSLATIONS)
	public Map<String, String> getTranslations() {
		return translations;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	public void setTranslations(final Map<String, String> translations) {
		this.translations = translations;
	}

}