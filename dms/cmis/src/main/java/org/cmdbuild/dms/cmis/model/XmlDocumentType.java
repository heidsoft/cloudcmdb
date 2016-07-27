package org.cmdbuild.dms.cmis.model;

import static javax.xml.bind.annotation.XmlAccessType.FIELD;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(FIELD)
public class XmlDocumentType extends XmlObject {

	@XmlAttribute(name = "name")
	private String name;

	@XmlElement(name = "group")
	private List<XmlMetadataGroup> groupList;

	public String getName() {
		return name;
	}

	void setName(final String name) {
		this.name = name;
	}

	public List<XmlMetadataGroup> getGroupList() {
		return groupList;
	}

	void setGroupList(final List<XmlMetadataGroup> groupList) {
		this.groupList = groupList;
	}

}