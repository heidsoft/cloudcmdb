package org.cmdbuild.servlets.json.serializers.translations.commons;

import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;

public interface TranslationSectionSerializer {
	
	//TODO: move somewhere else
	static final String FIELD = "field";
	static final String ELEMENT = "element";
	static final String DIRECTION = "direction";
	static final String PROCESS = "process";
	static final String CLASS = "class";
	static final String DOMAIN = "domain";
	static final String ATTRIBUTE = "attribute";

	Iterable<TranslationSerialization> serialize();

}
