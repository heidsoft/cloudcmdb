package org.cmdbuild.logic.translation;

import java.util.Collections;
import java.util.Map;

public class NullTranslationObject implements TranslationObject {

	private static final Map<String, String> NO_TRANSLATIONS = Collections.emptyMap();

	private static TranslationObject NULL_TRANSLATION_OBJECT;

	public static TranslationObject getInstance() {
		if (NULL_TRANSLATION_OBJECT == null) {
			NULL_TRANSLATION_OBJECT = new NullTranslationObject();
		}
		return NULL_TRANSLATION_OBJECT;
	}

	@Override
	public void accept(final TranslationObjectVisitor visitor) {
		visitor.visit(this);
	};

	@Override
	public Map<String, String> getTranslations() {
		return NO_TRANSLATIONS;
	}

	@Override
	public boolean isValid() {
		return false;
	}

}
