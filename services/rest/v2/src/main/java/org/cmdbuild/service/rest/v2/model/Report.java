package org.cmdbuild.service.rest.v2.model;

import static org.cmdbuild.service.rest.v2.constants.Serialization.DESCRIPTION;
import static org.cmdbuild.service.rest.v2.constants.Serialization.TITLE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.UNDERSCORED_ID;

import javax.xml.bind.annotation.XmlAttribute;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonProperty;

public class Report extends AbstractModel {

	private Long id;
	private String title;
	private String description;

	Report() {
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

	@XmlAttribute(name = TITLE)
	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	@XmlAttribute(name = DESCRIPTION)
	public String getDescription() {
		return description;
	}

	void setDescription(final String description) {
		this.description = description;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Report)) {
			return false;
		}

		final Report other = Report.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.id, other.id) //
				.append(this.title, other.title) //
				.append(this.description, other.description) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(this.id) //
				.append(this.description) //
				.toHashCode();
	}

}
