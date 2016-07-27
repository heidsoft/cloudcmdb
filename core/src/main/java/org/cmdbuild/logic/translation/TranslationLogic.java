package org.cmdbuild.logic.translation;

import java.util.Map;

import org.cmdbuild.logic.Logic;

public interface TranslationLogic extends Logic {
	
	@Deprecated
	void create(TranslationObject translationObject);

	Map<String, String> readAll(TranslationObject translationObject);

	String read(TranslationObject translationObject, String lang);

	void update(TranslationObject translationObject);

	void delete(TranslationObject translationObject);

}
