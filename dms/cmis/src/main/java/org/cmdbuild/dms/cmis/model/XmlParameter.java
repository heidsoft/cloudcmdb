package org.cmdbuild.dms.cmis.model;

import static javax.xml.bind.annotation.XmlAccessType.FIELD;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(FIELD)
public class XmlParameter extends XmlObject {

	@XmlAttribute(name = "name")
	private String name;

	@XmlAttribute(name = "value")
	private String value;

	public String getName() {
		return name;
	}

	void setName(final String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	void setValue(final String value) {
		this.value = value;
	}

}