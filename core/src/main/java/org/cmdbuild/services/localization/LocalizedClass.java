package org.cmdbuild.services.localization;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.ForwardingClass;
import org.cmdbuild.logic.translation.TranslationFacade;
import org.cmdbuild.logic.translation.converter.ClassConverter;

class LocalizedClass extends ForwardingClass {

	private final CMClass delegate;
	private final TranslationFacade facade;

	protected LocalizedClass(final CMClass delegate, final TranslationFacade facade) {
		this.delegate = delegate;
		this.facade = facade;
	}

	@Override
	protected CMClass delegate() {
		return delegate;
	}

	@Override
	public String getDescription() {
		return defaultIfBlank( //
				facade.read(ClassConverter.of(ClassConverter.description()) //
						.withIdentifier(getName()).create()), //
				super.getDescription());
	}

}
