package org.cmdbuild.servlets.json.serializers;

import org.cmdbuild.logic.translation.SetupFacade;
import org.cmdbuild.logic.translation.TranslationFacade;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.translation.TranslationObject;

public class DefaultTranslationFacade implements TranslationFacade {

	private final TranslationLogic translationLogic;
	private final SetupFacade setupFacade;

	public DefaultTranslationFacade(final TranslationLogic translationLogic, final SetupFacade setupFacade) {
		this.translationLogic = translationLogic;
		this.setupFacade = setupFacade;
	}

	@Override
	public String read(final TranslationObject translationObject) {
		final String output;
		if (setupFacade.isEnabled()) {
			final String language = setupFacade.getLocalization();
			output = translationLogic.readAll(translationObject).get(language);
		} else {
			output = null;
		}
		return output;
	}

}
