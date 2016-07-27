package org.cmdbuild.service.rest.v1.model;

import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_ID;
import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_TYPE;

import javax.xml.bind.annotation.XmlAttribute;

import org.codehaus.jackson.annotate.JsonProperty;

public abstract class AbstractCardModel extends AbstractModel {

	private Long id;
	private String type;

	protected AbstractCardModel() {
		// usable by subclasses only
	}

	@XmlAttribute(name = UNDERSCORED_ID)
	@JsonProperty(UNDERSCORED_ID)
	public Long getId() {
		return id;
	}

	void setId(final Long id) {
		this.id = id;
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
