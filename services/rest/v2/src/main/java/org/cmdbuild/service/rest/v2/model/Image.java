package org.cmdbuild.service.rest.v2.model;

import static org.cmdbuild.service.rest.v2.constants.Serialization.DETAILS;
import static org.cmdbuild.service.rest.v2.constants.Serialization.TYPE;

import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement
public class Image extends AbstractModel {

	public static final String filestore = "filestore";

	public static final String folder = "folder";
	public static final String file = "file";

	private String type;
	private Map<String, Object> details;

	Image() {
		// package visibility
	}

	@XmlAttribute(name = TYPE)
	public String getType() {
		return type;
	}

	void setType(final String type) {
		this.type = type;
	}

	@XmlElement(name = DETAILS)
	public Map<String, Object> getDetails() {
		return details;
	}

	void setDetails(final Map<String, Object> details) {
		this.details = details;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Image)) {
			return false;
		}

		final Image other = Image.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.getType(), other.getType()) //
				.append(this.getDetails(), other.getDetails()) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(getType()) //
				.append(getDetails()) //
				.toHashCode();
	}

}
