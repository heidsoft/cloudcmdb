package org.cmdbuild.services.localization;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import org.cmdbuild.logic.translation.TranslationFacade;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.ReportConverter;
import org.cmdbuild.services.store.report.ForwardingReport;
import org.cmdbuild.services.store.report.Report;

public class LocalizedReport extends ForwardingReport implements Report {

	private final Report delegate;
	private final TranslationFacade facade;

	LocalizedReport(final Report delegate, final TranslationFacade facade) {
		this.delegate = delegate;
		this.facade = facade;
	}

	@Override
	protected Report delegate() {
		return delegate;
	}

	@Override
	public String getDescription() {
		final TranslationObject translationObject = ReportConverter.of(ReportConverter.description()) //
				.withIdentifier(getCode()) //
				.create();
		final String translatedDescription = facade.read(translationObject);
		return defaultIfBlank(translatedDescription, super.getDescription());
	}

}
