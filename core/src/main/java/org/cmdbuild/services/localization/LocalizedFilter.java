package org.cmdbuild.services.localization;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import org.cmdbuild.logic.translation.TranslationFacade;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.FilterConverter;
import org.cmdbuild.services.store.filter.FilterStore.Filter;
import org.cmdbuild.services.store.filter.ForwardingFilter;

public class LocalizedFilter extends ForwardingFilter {

	private final Filter delegate;
	private final TranslationFacade facade;

	protected LocalizedFilter(final Filter delegate, final TranslationFacade facade) {
		this.delegate = delegate;
		this.facade = facade;
	}

	@Override
	protected Filter delegate() {
		return delegate;
	}

	@Override
	public String getDescription() {
		final TranslationObject translationObject = FilterConverter //
				.of(FilterConverter.description()) //
				.withIdentifier(getName())
				.create();
		final String translatedDescription = facade.read(translationObject);
		return defaultIfBlank(translatedDescription, super.getDescription());
	}

}
