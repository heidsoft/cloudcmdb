package org.cmdbuild.api.fluent;

import static com.google.common.base.Predicates.alwaysTrue;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.Collection;

import com.google.common.base.Predicate;

class AttachmentsImpl implements Attachments {

	private static final Attachment[] NO_ATTACHMENTS = new Attachment[] {};
	private static final String[] NO_NAMES = new String[] {};
	private static final Predicate<AttachmentDescriptor> ALL_ELEMENTS = alwaysTrue();

	private static class NamePredicate implements Predicate<AttachmentDescriptor> {

		private final Collection<String> allowed;

		public NamePredicate(final Iterable<String> names) {
			allowed = newArrayList(names);
		}

		@Override
		public boolean apply(final AttachmentDescriptor input) {
			return allowed.contains(input.getName());
		}

	}

	private final FluentApiExecutor executor;
	private final CardDescriptor descriptor;

	AttachmentsImpl(final FluentApiExecutor executor, final CardDescriptor descriptor) {
		this.executor = executor;
		this.descriptor = descriptor;
	}

	@Override
	public Iterable<AttachmentDescriptor> fetch() {
		return executor.fetchAttachments(descriptor);
	}

	@Override
	public void upload(final Attachment... attachments) {
		executor.upload(descriptor, newArrayList(defaultIfNull(attachments, NO_ATTACHMENTS)));
	}

	@Override
	public void upload(final String name, final String description, final String category, final String url) {
		executor.upload(descriptor, newArrayList(new AttachmentImpl(name, description, category, url)));
	}

	@Override
	public SelectedAttachments selectByName(final String... names) {
		return new SelectedAttachmentsImpl(executor, descriptor, new NamePredicate(newArrayList(defaultIfNull(names,
				NO_NAMES))));
	}

	@Override
	public SelectedAttachments selectAll() {
		return new SelectedAttachmentsImpl(executor, descriptor, ALL_ELEMENTS);
	}

}
