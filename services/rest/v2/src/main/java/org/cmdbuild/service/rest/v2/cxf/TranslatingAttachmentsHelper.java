package org.cmdbuild.service.rest.v2.cxf;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.service.rest.v2.model.Models.newAttachment;

import javax.activation.DataHandler;

import org.cmdbuild.service.rest.v2.model.Attachment;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ForwardingObject;

public class TranslatingAttachmentsHelper extends ForwardingObject implements AttachmentsHelper {

	public static interface Encoding {

		String encode(String value);

		String decode(String value);

	}

	private final Function<Attachment, Attachment> attachmentWithEncodedId = new Function<Attachment, Attachment>() {

		@Override
		public Attachment apply(final Attachment input) {
			return newAttachment(input) //
					.withId(encoding.encode(input.getId())) //
					.build();
		}

	};

	private final AttachmentsHelper delegate;
	private final Encoding encoding;

	public TranslatingAttachmentsHelper(final AttachmentsHelper delegate, final Encoding encoding) {
		this.delegate = delegate;
		this.encoding = encoding;
	}

	@Override
	protected AttachmentsHelper delegate() {
		return delegate;
	}

	@Override
	public String create(final String classId, final Long cardId, final String attachmentName,
			final Attachment attachment, final DataHandler dataHandler) throws Exception {
		return encoding.encode(delegate.create(classId, cardId, attachmentName, attachment, dataHandler));
	}

	@Override
	public void update(final String classId, final Long cardId, final String attachmentId, final Attachment attachment,
			final DataHandler dataHandler) throws Exception {
		delegate().update(classId, cardId, encoding.decode(attachmentId), attachment, dataHandler);
	}

	@Override
	public Iterable<Attachment> search(final String classId, final Long cardId) {
		return from(delegate().search(classId, cardId)) //
				.transform(attachmentWithEncodedId);
	}

	@Override
	public Optional<Attachment> search(final String classId, final Long cardId, final String attachmentId) {
		final Optional<Attachment> response = delegate().search(classId, cardId, encoding.decode(attachmentId));
		return response.isPresent() ? Optional.of(attachmentWithEncodedId.apply(response.get())) : response;
	}

	@Override
	public DataHandler download(final String classId, final Long cardId, final String attachmentId) {
		return delegate().download(classId, cardId, encoding.decode(attachmentId));
	}

	@Override
	public void delete(final String classId, final Long cardId, final String attachmentId) {
		delegate().delete(classId, cardId, encoding.decode(attachmentId));
	}

}
