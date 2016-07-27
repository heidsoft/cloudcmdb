package org.cmdbuild.service.rest.v2.model;

import static org.cmdbuild.service.rest.v2.constants.Serialization.UNDERSCORED_TYPE;

import javax.xml.bind.annotation.XmlAttribute;

import org.codehaus.jackson.annotate.JsonProperty;

public abstract class AbstractCardModel extends ModelWithLongId {

	private String type;

	protected AbstractCardModel() {
		// usable by subclasses only
	}

	@XmlAttribute(name = UNDERSCORED_TYPE)
	@JsonProperty(UNDERSCORED_TYPE)
	public String getType() {
		return type;
	}

	void setType(final String type) {
		this.type = type;
	}

}
