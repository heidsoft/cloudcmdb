package org.cmdbuild.service.rest.v1.model;

import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_ID;

import javax.xml.bind.annotation.XmlAttribute;

import org.codehaus.jackson.annotate.JsonProperty;

public abstract class ModelWithStringId extends AbstractModel {

	private String id;

	protected ModelWithStringId() {
		// usable by subclasses only
	}

	@XmlAttribute(name = UNDERSCORED_ID)
	@JsonProperty(UNDERSCORED_ID)
	public String getId() {
		return id;
	}

	void setId(final String id) {
		this.id = id;
	}

}
