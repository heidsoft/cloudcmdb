package org.cmdbuild.service.rest.v2.model;

import static org.cmdbuild.service.rest.v2.constants.Serialization.DESCRIPTION;
import static org.cmdbuild.service.rest.v2.constants.Serialization.NODES;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement
public class DomainTree extends ModelWithStringId {

	private String description;
	private Collection<Node> nodes;

	DomainTree() {
		// package visibility
	}

	@XmlAttribute(name = DESCRIPTION)
	public String getDescription() {
		return description;
	}

	void setDescription(final String description) {
		this.description = description;
	}

	@XmlElement(name = NODES, type = Node.class)
	public Collection<Node> getNodes() {
		return nodes;
	}

	void setNodes(final Collection<Node> nodes) {
		this.nodes = nodes;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof DomainTree)) {
			return false;
		}

		final DomainTree other = DomainTree.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.getId(), other.getId()) //
				.append(this.getDescription(), other.getDescription()) //
				.append(this.getNodes(), other.getNodes()) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(getId()) //
				.append(getDescription()) //
				.append(getNodes()) //
				.toHashCode();
	}

}
