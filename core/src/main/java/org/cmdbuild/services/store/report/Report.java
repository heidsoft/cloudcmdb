package org.cmdbuild.services.store.report;

import java.io.IOException;
import java.io.InputStream;

import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;

import org.cmdbuild.report.ReportFactory.ReportType;

public interface Report {

	ReportType getType();

	int getId();

	String getCode();

	String getDescription();

	String getUser();

	String getQuery();

	int getOriginalId();

	JasperDesign getJd();

	String[] getGroups();

	/**
	 * Get rich report as byte array
	 */
	byte[] getRichReportBA();

	/**
	 * Get rich report as JasperReport objects array
	 */
	JasperReport[] getRichReportJRA() throws ClassNotFoundException, IOException;

	byte[] getSimpleReport();

	byte[] getWizard();

	/**
	 * Get report images as byte array
	 */
	byte[] getImagesBA();

	/**
	 * Get report images as input stream array
	 */
	InputStream[] getImagesISA();

	Integer[] getReportLength();

	Integer[] getImagesLength();

	String[] getImagesName();

	int getSubreportsNumber();

	String[] getSubreportsName();

}