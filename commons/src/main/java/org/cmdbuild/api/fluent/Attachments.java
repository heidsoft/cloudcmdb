package org.cmdbuild.api.fluent;

public interface Attachments {

	Iterable<AttachmentDescriptor> fetch();

	void upload(Attachment... attachments);

	void upload(final String name, final String description, final String category, final String url);

	SelectedAttachments selectByName(String... names);

	// TODO add later
	// SelectedAttachments selectByRegex(String regex);

	SelectedAttachments selectAll();

}
