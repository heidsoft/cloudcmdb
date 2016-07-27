package org.cmdbuild.api.fluent.ws;

import org.cmdbuild.api.fluent.Attachment;

class AttachmentImpl extends AttachmentDescriptorImpl implements Attachment {

	private String url;

	public String getUrl() {
		return url;
	}

	void setUrl(final String url) {
		this.url = url;
	}

}
