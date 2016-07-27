package org.cmdbuild.servlets.json.serializers.translations.csv.read;

import org.cmdbuild.logic.translation.NullTranslationObject;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;

public class SafeRecordDeserializer extends ForwardingRecordDeserializer {

	private final RecordDeserializer delegate;
	private ErrorListener listener;

	public static SafeRecordDeserializer of(final RecordDeserializer delegate) {
		return new SafeRecordDeserializer(delegate);
	}

	public RecordDeserializer withErrorListener(final ErrorListener listener) {
		this.listener = listener;
		return this;
	}

	@Override
	protected RecordDeserializer delegate() {
		return delegate;
	}

	private SafeRecordDeserializer(final RecordDeserializer delegate) {
		this.delegate = delegate;
	}

	@Override
	public TranslationObject deserialize() {
		final TranslationSerialization record = getInput();
		TranslationObject output = NullTranslationObject.getInstance();
		try {
			output = delegate().deserialize();
		} catch (final Throwable throwable) {
			listener.handleError(record, throwable);
		}
		return output;
	}

}
