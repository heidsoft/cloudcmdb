package org.cmdbuild.logic.translation.converter;

import java.util.Map;

import org.cmdbuild.logic.translation.TranslationObject;

public interface Converter {

	public boolean isValid();

	public TranslationObject create();

	public Converter withOwner(String parentIdentifier);

	public Converter withIdentifier(String identifier);

	public Converter withTranslations(Map<String, String> map);

}
