package org.cmdbuild.servlets.json.serializers.translations.csv.read;

import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingRecordDeserializer extends ForwardingObject implements RecordDeserializer {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingRecordDeserializer() {
	}

	@Override
	protected abstract RecordDeserializer delegate();

	@Override
	public TranslationObject deserialize() {
		return delegate().deserialize();
	}

	@Override
	public TranslationSerialization getInput() {
		return delegate().getInput();
	}

}
