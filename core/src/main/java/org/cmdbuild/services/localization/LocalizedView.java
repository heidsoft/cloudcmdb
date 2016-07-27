package org.cmdbuild.services.localization;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import org.cmdbuild.logic.translation.TranslationFacade;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.ViewConverter;
import org.cmdbuild.model.view.ForwardingView;
import org.cmdbuild.model.view.View;

public class LocalizedView extends ForwardingView {

	private final View delegate;
	private final TranslationFacade facade;

	public LocalizedView(final View delegate, final TranslationFacade facade) {
		this.delegate = delegate;
		this.facade = facade;
	}

	@Override
	public void accept(final LocalizableStorableVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected View delegate() {
		return delegate;
	}

	@Override
	public String getDescription() {
		final TranslationObject translationObject = ViewConverter.of(ViewConverter.description()) //
				.withIdentifier(getName())
				.create();
		final String translatedDescription = facade.read(translationObject);
		return defaultIfBlank(translatedDescription, super.getDescription());
	}

}
