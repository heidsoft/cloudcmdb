package org.cmdbuild.logic.translation;

import java.util.Map;

public interface TranslationObject {

	void accept(TranslationObjectVisitor visitor);

	Map<String, String> getTranslations();

	boolean isValid();

}
