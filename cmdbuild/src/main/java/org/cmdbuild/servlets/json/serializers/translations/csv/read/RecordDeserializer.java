package org.cmdbuild.servlets.json.serializers.translations.csv.read;

import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;

public interface RecordDeserializer {

	TranslationObject deserialize();

	TranslationSerialization getInput();

}
