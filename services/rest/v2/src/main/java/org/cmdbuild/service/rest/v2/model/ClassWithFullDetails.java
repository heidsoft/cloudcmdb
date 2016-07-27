package org.cmdbuild.service.rest.v2.model;

import static org.cmdbuild.service.rest.v2.constants.Serialization.ATTRIBUTE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.DEFAULT_ORDER;
import static org.cmdbuild.service.rest.v2.constants.Serialization.DESCRIPTION_ATTRIBUTE_NAME;
import static org.cmdbuild.service.rest.v2.constants.Serialization.DIRECTION;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement
public class ClassWithFullDetails extends ClassWithBasicDetails {

	@XmlRootElement
	public static class AttributeOrder extends AbstractModel {

		private String attribute;
		private String direction;

		AttributeOrder() {
			// package visibility
		}

		@XmlAttribute(name = ATTRIBUTE)
		public String getAttribute() {
			return attribute;
		}

		void setAttribute(final String attribute) {
			this.attribute = attribute;
		}

		@XmlAttribute(name = DIRECTION)
		public String getDirection() {
			return direction;
		}

		void setDirection(final String direction) {
			this.direction = direction;
		}

		@Override
		protected boolean doEquals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof AttributeOrder)) {
				return false;
			}
			final AttributeOrder other = AttributeOrder.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.attribute, other.attribute) //
					.append(this.direction, other.direction) //
					.isEquals();
		}

		@Override
		protected int doHashCode() {
			return new HashCodeBuilder() //
					.append(attribute) //
					.append(direction) //
					.toHashCode();
		}

	}

	private String descriptionAttributeName;
	private Collection<AttributeOrder> order;

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

	@XmlElement(name = DEFAULT_ORDER, nillable = true)
	public Collection<AttributeOrder> getDefaultOrder() {
		return order;
	}

	void setDefaultOrder(final Collection<AttributeOrder> order) {
		this.order = order;
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
