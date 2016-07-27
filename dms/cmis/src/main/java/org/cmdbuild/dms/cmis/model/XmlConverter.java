package org.cmdbuild.dms.cmis.model;

import static javax.xml.bind.annotation.XmlAccessType.FIELD;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(FIELD)
public class XmlConverter extends XmlObject {

	@XmlAttribute(name = "type")
	private String type;

	@XmlElement(name = "property")
	private List<String> cmisPropertyId;

	public String getType() {
		return type;
	}

	void setType(final String type) {
		this.type = type;
	}

	public List<String> getCmisPropertyId() {
		return cmisPropertyId;
	}

	void setCmisPropertyId(final List<String> cmisPropertyId) {
		this.cmisPropertyId = cmisPropertyId;
	}

}