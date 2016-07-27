package org.cmdbuild.servlets.json.serializers;

import org.cmdbuild.logic.translation.TranslationFacade;
import org.cmdbuild.logic.translation.TranslationObject;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingTranslationFacade extends ForwardingObject implements TranslationFacade {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingTranslationFacade() {
	}

	@Override
	protected abstract TranslationFacade delegate();

	@Override
	public String read(final TranslationObject translationObject) {
		return delegate().read(translationObject);
	}

}
