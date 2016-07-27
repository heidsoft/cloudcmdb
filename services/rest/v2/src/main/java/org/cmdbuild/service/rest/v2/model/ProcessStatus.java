package org.cmdbuild.service.rest.v2.model;

import static org.cmdbuild.service.rest.v2.constants.Serialization.DESCRIPTION;
import static org.cmdbuild.service.rest.v2.constants.Serialization.UNDERSCORED_ID;
import static org.cmdbuild.service.rest.v2.constants.Serialization.VALUE;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement
public class ProcessStatus extends AbstractModel {

	public static final String OPEN = "open";
	public static final String SUSPENDED = "suspended";
	public static final String COMPLETED = "completed";
	public static final String ABORTED = "closed";

	private Long id;
	private String value;
	private String desctipion;

	ProcessStatus() {
		// package visibility
	}

	@XmlAttribute(name = UNDERSCORED_ID)
	@JsonProperty(UNDERSCORED_ID)
	public Long getId() {
		return id;
	}

	void setId(final Long id) {
		this.id = id;
	}

	@XmlAttribute(name = VALUE)
	public String getValue() {
		return value;
	}

	void setValue(final String value) {
		this.value = value;
	}

	@XmlAttribute(name = DESCRIPTION)
	public String getDescription() {
		return desctipion;
	}

	void setDescription(final String desctipion) {
		this.desctipion = desctipion;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof ProcessStatus)) {
			return false;
		}

		final ProcessStatus other = ProcessStatus.class.cast(obj);

		return new EqualsBuilder() //
				.append(this.getId(), other.getId()) //
				.append(this.value, other.value) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(this.getId()) //
				.append(this.value) //
				.toHashCode();
	}

}
