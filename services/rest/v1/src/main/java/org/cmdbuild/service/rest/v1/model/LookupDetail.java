package org.cmdbuild.service.rest.v1.model;

import static org.cmdbuild.service.rest.v1.constants.Serialization.ACTIVE;
import static org.cmdbuild.service.rest.v1.constants.Serialization.CODE;
import static org.cmdbuild.service.rest.v1.constants.Serialization.DEFAULT;
import static org.cmdbuild.service.rest.v1.constants.Serialization.DESCRIPTION;
import static org.cmdbuild.service.rest.v1.constants.Serialization.NUMBER;
import static org.cmdbuild.service.rest.v1.constants.Serialization.PARENT_ID;
import static org.cmdbuild.service.rest.v1.constants.Serialization.PARENT_TYPE;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement
public class LookupDetail extends AbstractCardModel {

	private String code;
	private String description;
	private Long number;
	private Boolean active;
	private Boolean isDefault;
	private Long parentId;
	private String parentType;

	LookupDetail() {
		// package visibility
	}

	@XmlAttribute(name = CODE)
	public String getCode() {
		return code;
	}

	void setCode(final String code) {
		this.code = code;
	}

	@XmlAttribute(name = DESCRIPTION)
	public String getDescription() {
		return description;
	}

	void setDescription(final String description) {
		this.description = description;
	}

	@XmlAttribute(name = NUMBER)
	public Long getNumber() {
		return number;
	}

	void setNumber(final Long number) {
		this.number = number;
	}

	@XmlAttribute(name = ACTIVE)
	public Boolean isActive() {
		return active;
	}

	void setActive(final Boolean active) {
		this.active = active;
	}

	@XmlAttribute(name = DEFAULT)
	public Boolean isDefault() {
		return isDefault;
	}

	void setDefault(final Boolean isDefault) {
		this.isDefault = isDefault;
	}

	@XmlAttribute(name = PARENT_ID)
	@JsonProperty(PARENT_ID)
	public Long getParentId() {
		return parentId;
	}

	void setParentId(final Long parentId) {
		this.parentId = parentId;
	}

	@XmlAttribute(name = PARENT_TYPE)
	@JsonProperty(PARENT_TYPE)
	public String getParentType() {
		return parentType;
	}

	void setParentType(final String parentType) {
		this.parentType = parentType;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof LookupDetail)) {
			return false;
		}

		final LookupDetail other = LookupDetail.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.getType(), other.getType()) //
				.append(this.getId(), other.getId()) //
				.append(this.code, other.code) //
				.append(this.description, other.description) //
				.append(this.number, other.number) //
				.append(this.active, other.active) //
				.append(this.isDefault, other.isDefault) //
				.append(this.parentId, other.parentId) //
				.append(this.parentType, other.parentType) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(getType()) //
				.append(getId()) //
				.append(code) //
				.append(description) //
				.append(number) //
				.append(active) //
				.append(isDefault) //
				.append(parentId) //
				.append(parentType) //
				.toHashCode();
	}

}
