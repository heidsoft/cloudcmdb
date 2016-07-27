package org.cmdbuild.dms.cmis.model;

import static javax.xml.bind.annotation.XmlAccessType.FIELD;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlList;

@XmlAccessorType(FIELD)
public class XmlModel extends XmlObject {

	@XmlAttribute(name = "id")
	private String id;

	@XmlAttribute(name = "description")
	private String description;

	@XmlAttribute(name = "type")
	private String cmisType;

	@XmlList
	@XmlAttribute(name = "secondary-types")
	private List<String> secondaryTypeList;

	@XmlElementWrapper(name = "document-types")
	@XmlElement(name = "document-type")
	private List<XmlDocumentType> documentTypeList;

	@XmlElementWrapper(name = "property-converters")
	@XmlElement(name = "converter")
	private List<XmlConverter> converterList;

	@XmlElement(name = "author")
	private String author;

	@XmlElement(name = "category")
	private String category;

	@XmlElement(name = "description")
	private String descriptionProperty;

	@XmlElementWrapper(name = "session-parameters")
	@XmlElement(name = "parameter")
	private List<XmlParameter> sessionParameters;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getCmisType() {
		return cmisType;
	}

	void setCmisType(final String cmisType) {
		this.cmisType = cmisType;
	}

	public List<String> getSecondaryTypeList() {
		return secondaryTypeList;
	}

	void setSecondaryTypeList(final List<String> secondaryTypeList) {
		this.secondaryTypeList = secondaryTypeList;
	}

	public List<XmlDocumentType> getDocumentTypeList() {
		return documentTypeList;
	}

	void setDocumentTypeList(final List<XmlDocumentType> documentTypeList) {
		this.documentTypeList = documentTypeList;
	}

	public List<XmlConverter> getConverterList() {
		return converterList;
	}

	void setConverterList(final List<XmlConverter> converterList) {
		this.converterList = converterList;
	}

	public String getAuthor() {
		return author;
	}

	void setAuthor(final String author) {
		this.author = author;
	}

	public String getCategory() {
		return category;
	}

	void setCategory(final String category) {
		this.category = category;
	}

	public String getDescriptionProperty() {
		return descriptionProperty;
	}

	void setDescriptionProperty(final String descriptionProperty) {
		this.descriptionProperty = descriptionProperty;
	}

	public List<XmlParameter> getSessionParameters() {
		return sessionParameters;
	}

	void setSessionParameters(final List<XmlParameter> sessionParameters) {
		this.sessionParameters = sessionParameters;
	}

}
