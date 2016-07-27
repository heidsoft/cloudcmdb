package org.cmdbuild.service.rest.v2.model;

import static org.cmdbuild.service.rest.v2.constants.Serialization.DETAILS;
import static org.cmdbuild.service.rest.v2.constants.Serialization.ID;
import static org.cmdbuild.service.rest.v2.constants.Serialization.IMAGE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.TYPE;

import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement
public class Icon extends ModelWithLongId {

	public static final String id = ID;

	private String type;
	private Map<String, Object> details;
	private Image image;

	Icon() {
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

	@XmlElement(name = IMAGE)
	public Image getImage() {
		return image;
	}

	void setImage(final Image image) {
		this.image = image;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Icon)) {
			return false;
		}

		final Icon other = Icon.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.getId(), other.getId()) //
				.append(this.getType(), other.getType()) //
				.append(this.getDetails(), other.getDetails()) //
				.append(this.getImage(), other.getImage()) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(getId()) //
				.append(getType()) //
				.append(getDetails()) //
				.append(getImage()) //
				.toHashCode();
	}

}
