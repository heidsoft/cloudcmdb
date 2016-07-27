package org.cmdbuild.service.rest.v1.model;

import static org.cmdbuild.service.rest.v1.constants.Serialization.DEFAULT_STATUS;
import static org.cmdbuild.service.rest.v1.constants.Serialization.DESCRIPTION_ATTRIBUTE_NAME;
import static org.cmdbuild.service.rest.v1.constants.Serialization.STATUSES;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement
public class ProcessWithFullDetails extends ProcessWithBasicDetails {

	private String descriptionAttributeName;
	private Collection<Long> statuses;
	private Long defaultStatus;

	ProcessWithFullDetails() {
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

	@XmlAttribute(name = STATUSES)
	public Collection<Long> getStatuses() {
		return statuses;
	}

	void setStatuses(final Collection<Long> statuses) {
		this.statuses = statuses;
	}

	@XmlAttribute(name = DEFAULT_STATUS)
	public Long getDefaultStatus() {
		return defaultStatus;
	}

	void setDefaultStatus(final Long defaultStatus) {
		this.defaultStatus = defaultStatus;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof ProcessWithFullDetails)) {
			return false;
		}

		final ProcessWithFullDetails other = ProcessWithFullDetails.class.cast(obj);
		return super.doEquals(other) && new EqualsBuilder() //
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
