package org.cmdbuild.api.fluent;

class AttachmentImpl implements Attachment {

	private final String url;
	private final String name;
	private final String category;
	private final String description;

	AttachmentImpl(final String name, final String description, final String category, final String url) {
		this.url = url;
		this.name = name;
		this.category = category;
		this.description = description;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getCategory() {
		return category;
	}

	@Override
	public String getUrl() {
		return url;
	}

}