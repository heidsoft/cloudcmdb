package org.cmdbuild.service.rest.v2.model;

import static org.cmdbuild.service.rest.v2.constants.Serialization.UNDERSCORED_ID;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement
public class LongId extends AbstractModel {

	private Long id;

	LongId() {
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

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof LongId)) {
			return false;
		}

		final LongId other = LongId.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.id, other.id) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(this.id) //
				.toHashCode();
	}

}
