package org.cmdbuild.service.rest.v2.model;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement
public class FileSystemObject extends ModelWithStringId {

	private String name;
	private String parent;

	FileSystemObject() {
		// package visibility
	}

	public String getName() {
		return name;
	}

	void setName(final String name) {
		this.name = name;
	}

	public String getParent() {
		return parent;
	}

	void setParent(final String parent) {
		this.parent = parent;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof FileSystemObject)) {
			return false;
		}

		final FileSystemObject other = FileSystemObject.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.getId(), other.getId()) //
				.append(this.getName(), other.getName()) //
				.append(this.getParent(), other.getParent()) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(getId()) //
				.append(getId()) //
				.append(getParent()) //
				.toHashCode();
	}

}
