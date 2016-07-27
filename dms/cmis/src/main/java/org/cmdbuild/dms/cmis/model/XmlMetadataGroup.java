package org.cmdbuild.dms.cmis.model;

import static javax.xml.bind.annotation.XmlAccessType.FIELD;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(FIELD)
public class XmlMetadataGroup extends XmlObject {

	@XmlAttribute(name = "name")
	private String name;

	@XmlAttribute(name = "secondary-type")
	private String cmisSecondaryTypeId;

	@XmlElement(name = "metadata")
	private List<XmlMetadata> metadataList;

	public String getName() {
		return name;
	}

	void setName(final String name) {
		this.name = name;
	}

	public String getCmisSecondaryTypeId() {
		return cmisSecondaryTypeId;
	}

	void setCmisSecondaryTypeId(final String cmisSecondaryTypeId) {
		this.cmisSecondaryTypeId = cmisSecondaryTypeId;
	}

	public List<XmlMetadata> getMetadataList() {
		return metadataList;
	}

	void setMetadataList(final List<XmlMetadata> metadataList) {
		this.metadataList = metadataList;
	}

}