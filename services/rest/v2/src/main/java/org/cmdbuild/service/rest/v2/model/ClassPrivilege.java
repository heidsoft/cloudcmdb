package org.cmdbuild.service.rest.v2.model;

import static org.cmdbuild.service.rest.v2.constants.Serialization.DESCRIPTION;
import static org.cmdbuild.service.rest.v2.constants.Serialization.MODE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.NAME;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement
public class ClassPrivilege extends ModelWithStringId {

	private String name;
	private String description;
	private String mode;

	ClassPrivilege() {
		// package visibility
	}

	@XmlAttribute(name = NAME)
	public String getName() {
		return name;
	}

	void setName(final String name) {
		this.name = name;
	}

	@XmlAttribute(name = DESCRIPTION)
	public String getDescription() {
		return description;
	}

	void setDescription(final String description) {
		this.description = description;
	}

	@XmlAttribute(name = MODE)
	public String getMode() {
		return mode;
	}

	void setMode(final String mode) {
		this.mode = mode;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof ClassPrivilege)) {
			return false;
		}

		final ClassPrivilege other = ClassPrivilege.class.cast(obj);

		return new EqualsBuilder() //
				.append(this.getId(), other.getId()) //
				.append(this.name, other.name) //
				.append(this.description, other.description) //
				.append(this.mode, other.mode) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(this.getId()) //
				.append(this.name) //
				.append(this.description) //
				.append(this.mode) //
				.toHashCode();
	}

}
