package org.cmdbuild.data.store.translation;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.data.store.Storable;

public class Translation implements Storable {

	private final Long id;

	private String element;
	private String lang;
	private String value;

	public Translation() {
		this(null);
	}

	public Translation(final Long id) {
		this.id = id;
	}

	@Override
	public String getIdentifier() {
		return id.toString();
	}

	public Long getId() {
		return id;
	}

	public String getElement() {
		return element;
	}

	public void setElement(final String element) {
		this.element = element;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(final String lang) {
		this.lang = lang;
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
	}

}
