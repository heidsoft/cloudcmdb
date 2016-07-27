package org.cmdbuild.services.localization;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.ForwardingDomain;
import org.cmdbuild.logic.translation.TranslationFacade;
import org.cmdbuild.logic.translation.converter.DomainConverter;

class LocalizedDomain extends ForwardingDomain {

	private final CMDomain delegate;
	private final TranslationFacade facade;

	private static final String DESCRIPTION = DomainConverter.DESCRIPTION.field();
	private static final String DIRECT_DESCRIPTION = DomainConverter.DIRECT_DESCRIPTION.field();
	private static final String INVERSE_DESCRIPTION = DomainConverter.INVERSE_DESCRIPTION.field();
	private static final String MASTERDETAIL_LABEL = DomainConverter.MASTERDETAIL_LABEL.field();

	protected LocalizedDomain(final CMDomain delegate, final TranslationFacade facade) {
		this.delegate = delegate;
		this.facade = facade;
	}

	@Override
	protected CMDomain delegate() {
		return delegate;
	}

	@Override
	public String getDescription() {

		return defaultIfBlank( //
				facade.read(DomainConverter.of(DESCRIPTION) //
						.withIdentifier(getName())
						.create()), //
				super.getDescription());
	}

	@Override
	public String getDescription1() {
		return defaultIfBlank( //
				facade.read(DomainConverter.of(DIRECT_DESCRIPTION) //
						.withIdentifier(getName())
						.create()), //
				super.getDescription1());
	}

	@Override
	public String getDescription2() {
		return defaultIfBlank( //
				facade.read(DomainConverter.of(INVERSE_DESCRIPTION) //
						.withIdentifier(getName())
						.create()), //
				super.getDescription2());
	}

	@Override
	public String getMasterDetailDescription() {
		return defaultIfBlank( //
				facade.read(DomainConverter.of(MASTERDETAIL_LABEL) //
						.withIdentifier(getName())
						.create()), //
				super.getMasterDetailDescription());
	}

}
