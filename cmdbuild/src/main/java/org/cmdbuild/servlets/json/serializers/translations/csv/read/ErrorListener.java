package org.cmdbuild.servlets.json.serializers.translations.csv.read;

import java.util.Map;

import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;


public interface ErrorListener {
	
	void handleError(TranslationSerialization input, Throwable throwable);

	Map<TranslationSerialization, Throwable> getFailures();
	
}
