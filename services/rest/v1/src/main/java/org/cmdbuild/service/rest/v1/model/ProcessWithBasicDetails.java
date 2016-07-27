package org.cmdbuild.service.rest.v1.model;

import static org.cmdbuild.service.rest.v1.constants.Serialization.DESCRIPTION;
import static org.cmdbuild.service.rest.v1.constants.Serialization.NAME;
import static org.cmdbuild.service.rest.v1.constants.Serialization.PARENT;
import static org.cmdbuild.service.rest.v1.constants.Serialization.PROTOTYPE;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement
public class ProcessWithBasicDetails extends ModelWithStringId {

	private String name;
	private String description;
	private String parent;
	private boolean prototype;

	ProcessWithBasicDetails() {
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

	@XmlAttribute(name = PARENT)
	public String getParent() {
		return parent;
	}

	void setParent(final String parent) {
		this.parent = parent;
	}

	@XmlAttribute(name = PROTOTYPE)
	public boolean isPrototype() {
		return prototype;
	}

	void setPrototype(final boolean prototype) {
		this.prototype = prototype;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof ProcessWithBasicDetails)) {
			return false;
		}

		final ProcessWithBasicDetails other = ProcessWithBasicDetails.class.cast(obj);

		return new EqualsBuilder() //
				.append(this.getId(), other.getId()) //
				.append(this.name, other.name) //
				.append(this.description, other.description) //
				.append(this.parent, other.parent) //
				.append(this.prototype, other.prototype) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(this.getId()) //
				.append(this.name) //
				.append(this.description) //
				.append(this.parent) //
				.append(this.prototype) //
				.toHashCode();
	}

}
