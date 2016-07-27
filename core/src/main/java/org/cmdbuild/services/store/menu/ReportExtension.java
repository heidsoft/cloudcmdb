package org.cmdbuild.services.store.menu;

import org.cmdbuild.report.ReportFactory;

public enum ReportExtension {

	PDF(ReportFactory.ReportExtension.PDF.toString().toLowerCase()), //
	CSV(ReportFactory.ReportExtension.CSV.toString().toLowerCase()), //
	;

	private final String extension;

	private ReportExtension(final String extension) {
		this.extension = extension;
	}

	public String getExtension() {
		return this.extension;
	}

}