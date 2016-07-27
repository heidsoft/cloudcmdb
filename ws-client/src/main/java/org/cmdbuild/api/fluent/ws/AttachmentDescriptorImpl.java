package org.cmdbuild.api.fluent.ws;

import org.cmdbuild.api.fluent.AttachmentDescriptor;

class AttachmentDescriptorImpl implements AttachmentDescriptor {

	private String name;
	private String description;
	private String category;

	public String getName() {
		return name;
	}

	void setName(final String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	void setDescription(final String description) {
		this.description = description;
	}

	public String getCategory() {
		return category;
	}

	void setCategory(final String category) {
		this.category = category;
	}

}
