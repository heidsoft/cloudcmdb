package org.cmdbuild.service.rest.v1.model;

import static org.cmdbuild.service.rest.v1.constants.Serialization.DESCRIPTION_ATTRIBUTE_NAME;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement
public class ClassWithFullDetails extends ClassWithBasicDetails {

	private String descriptionAttributeName;

	ClassWithFullDetails() {
		// package visibility
	}

	@XmlAttribute(name = DESCRIPTION_ATTRIBUTE_NAME)
	@JsonProperty(DESCRIPTION_ATTRIBUTE_NAME)
	public String getDescriptionAttributeName() {
		return descriptionAttributeName;
	}

	void setDescriptionAttributeName(final String descriptionAttributeName) {
		this.descriptionAttributeName = descriptionAttributeName;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof ClassWithFullDetails)) {
			return false;
		}

		final ClassWithFullDetails other = ClassWithFullDetails.class.cast(obj);
		return super.doEquals(obj) && new EqualsBuilder() //
				.append(this.descriptionAttributeName, other.descriptionAttributeName) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(super.doHashCode()) //
				.append(this.descriptionAttributeName) //
				.toHashCode();
	}

}
