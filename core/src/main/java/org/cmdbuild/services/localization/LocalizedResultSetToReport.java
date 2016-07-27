package org.cmdbuild.services.localization;

import java.sql.ResultSet;

import org.cmdbuild.logic.translation.TranslationFacade;
import org.cmdbuild.services.store.report.Report;

import com.google.common.base.Function;

public class LocalizedResultSetToReport implements Function<ResultSet, Report> {

	private final Function<ResultSet, Report> delegate;
	private final TranslationFacade facade;

	public LocalizedResultSetToReport(final Function<ResultSet, Report> delegate, final TranslationFacade facade) {
		this.delegate = delegate;
		this.facade = facade;
	}

	@Override
	public Report apply(final ResultSet input) {
		return proxy(delegate.apply(input));
	}

	private Report proxy(final Report input) {
		return new LocalizedReport(input, facade);
	}

}
