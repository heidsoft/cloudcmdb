package org.cmdbuild.services.store.report;

import java.io.IOException;
import java.io.InputStream;

import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;

import org.cmdbuild.report.ReportFactory.ReportType;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingReport extends ForwardingObject implements Report {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingReport() {
	}

	@Override
	protected abstract Report delegate();

	@Override
	public ReportType getType() {
		return delegate().getType();
	}

	@Override
	public int getId() {
		return delegate().getId();
	}

	@Override
	public String getCode() {
		return delegate().getCode();
	}

	@Override
	public String getDescription() {
		return delegate().getDescription();
	}

	@Override
	public String getUser() {
		return delegate().getUser();
	}

	@Override
	public String getQuery() {
		return delegate().getQuery();
	}

	@Override
	public int getOriginalId() {
		return delegate().getOriginalId();
	}

	@Override
	public JasperDesign getJd() {
		return delegate().getJd();
	}

	@Override
	public String[] getGroups() {
		return delegate().getGroups();
	}

	@Override
	public byte[] getRichReportBA() {
		return delegate().getRichReportBA();
	}

	@Override
	public JasperReport[] getRichReportJRA() throws ClassNotFoundException, IOException {
		return delegate().getRichReportJRA();
	}

	@Override
	public byte[] getSimpleReport() {
		return delegate().getSimpleReport();
	}

	@Override
	public byte[] getWizard() {
		return delegate().getWizard();
	}

	@Override
	public byte[] getImagesBA() {
		return delegate().getImagesBA();
	}

	@Override
	public InputStream[] getImagesISA() {
		return delegate().getImagesISA();
	}

	@Override
	public Integer[] getReportLength() {
		return delegate().getReportLength();
	}

	@Override
	public Integer[] getImagesLength() {
		return delegate().getImagesLength();
	}

	@Override
	public String[] getImagesName() {
		return delegate().getImagesName();
	}

	@Override
	public int getSubreportsNumber() {
		return delegate().getSubreportsNumber();
	}

	@Override
	public String[] getSubreportsName() {
		return delegate().getSubreportsName();
	}

}
