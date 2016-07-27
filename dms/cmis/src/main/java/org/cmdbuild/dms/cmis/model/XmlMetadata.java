package org.cmdbuild.dms.cmis.model;

import static javax.xml.bind.annotation.XmlAccessType.FIELD;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(FIELD)
public class XmlMetadata extends XmlObject {

	@XmlAttribute(name = "name")
	private String name;

	@XmlAttribute(name = "property")
	private String cmisPropertyId;

	public String getName() {
		return name;
	}

	void setName(final String name) {
		this.name = name;
	}

	public String getCmisPropertyId() {
		return cmisPropertyId;
	}

	void setCmisPropertyId(final String cmisPropertyId) {
		this.cmisPropertyId = cmisPropertyId;
	}

}