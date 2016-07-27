package org.cmdbuild.service.rest.v2.model;

import static org.cmdbuild.service.rest.v2.constants.Serialization.METADATA;
import static org.cmdbuild.service.rest.v2.constants.Serialization.PARENT;

import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement
public class Node extends ModelWithLongId {

	private Long parent;
	private Map<String, Object> metadata;

	Node() {
		// package visibility
	}

	@XmlAttribute(name = PARENT)
	public Long getParent() {
		return parent;
	}

	void setParent(final Long parent) {
		this.parent = parent;
	}

	@XmlElement(name = METADATA)
	public Map<String, Object> getMetadata() {
		return metadata;
	}

	void setMetadata(final Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Node)) {
			return false;
		}

		final Node other = Node.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.getId(), other.getId()) //
				.append(this.parent, other.parent) //
				.append(this.metadata, other.metadata) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(getId()) //
				.append(parent) //
				.append(metadata) //
				.toHashCode();
	}

}
