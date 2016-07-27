package org.cmdbuild.services.store.report;

import static org.cmdbuild.utils.BinaryUtils.fromByte;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;

import org.cmdbuild.report.ReportFactory.ReportType;

class ReportImpl implements Report {

	public static final String REPORT_CLASS_NAME = "Report";

	private int id = 0;
	private String code = "";
	private String description = "";
	private String user = "";

	private String query = "";
	private byte[] simpleReport = new byte[0];
	private byte[] richReport = new byte[0];
	private byte[] wizard = new byte[0];
	private byte[] images = new byte[0];
	private Integer[] imagesLength = new Integer[0];
	private Integer[] reportLength = new Integer[0];
	private String[] imagesName = new String[0];
	private String[] subreportsName = new String[0];
	private String[] groups = new String[0];

	/**
	 * id of the report we're editing, "-1" if it's a new one (administration
	 * side)
	 */
	private int originalId = -1;

	/**
	 * number of subreport elements (administration side)
	 */
	private int subreportsNumber = -1;

	/**
	 * jasper design created from uploaded file (administration side)
	 */
	private JasperDesign jasperDesign = null;

	@Override
	public ReportType getType() {
		// There is only one type
		// return it
		return ReportType.CUSTOM;
	}

	public void setType(final ReportType type) {
		// Do nothing
		// there is only one report type
	}

	@Override
	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	@Override
	public String getCode() {
		return code;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	@Override
	public String getUser() {
		return user;
	}

	public void setUser(final String user) {
		this.user = user;
	}

	@Override
	public String getQuery() {
		return query;
	}

	public void setQuery(final String query) {
		this.query = query;
	}

	@Override
	public int getOriginalId() {
		return originalId;
	}

	public void setOriginalId(final int originalId) {
		this.originalId = originalId;
	}

	@Override
	public JasperDesign getJd() {
		return jasperDesign;
	}

	public void setJd(final JasperDesign jasperDesign) {
		this.jasperDesign = jasperDesign;
	}

	public void setGroups(final String[] groupNames) {
		this.groups = groupNames;
	}

	@Override
	public String[] getGroups() {
		return groups;
	}

	/**
	 * Get rich report as byte array
	 */
	@Override
	public byte[] getRichReportBA() {
		return richReport;
	}

	/**
	 * Get rich report as JasperReport objects array
	 */
	@Override
	public JasperReport[] getRichReportJRA() throws ClassNotFoundException, IOException {
		int parseLength = 0;
		byte[] singleBin = null;
		final Integer[] length = getReportLength();
		final JasperReport[] obj = new JasperReport[length.length];
		final byte[] bin = getRichReportBA();

		// splits the reports in master and subreports
		for (int i = 0; i < length.length; i++) {
			singleBin = new byte[length[i]];
			for (int j = 0; j < length[i]; j++) {
				singleBin[j] = bin[parseLength + j];
			}
			parseLength += length[i];
			if (singleBin != null && length[i] > 0) {
				obj[i] = (JasperReport) fromByte(singleBin);
			}
		}

		return obj;
	}

	public void setRichReport(final byte[] richReport) {
		this.richReport = richReport;
	}

	@Override
	public byte[] getSimpleReport() {
		return simpleReport;
	}

	public void setSimpleReport(final byte[] simpleReport) {
		this.simpleReport = simpleReport;
	}

	@Override
	public byte[] getWizard() {
		return wizard;
	}

	public void setWizard(final byte[] wizard) {
		this.wizard = wizard;
	}

	/**
	 * Get report images as byte array
	 */
	@Override
	public byte[] getImagesBA() {
		return images;
	}

	/**
	 * Get report images as input stream array
	 */
	@Override
	public InputStream[] getImagesISA() {
		final byte[] binary = getImagesBA();
		final Integer[] imagesLength = getImagesLength();
		InputStream[] obj = new InputStream[0];
		if (imagesLength != null) {
			obj = new InputStream[imagesLength.length];
			int parseLength = 0;
			byte[] singleBin = null;

			// splits the images
			for (int i = 0; i < imagesLength.length; i++) {
				singleBin = new byte[imagesLength[i]];
				for (int j = 0; j < imagesLength[i]; j++) {
					singleBin[j] = binary[parseLength + j];
				}
				parseLength += imagesLength[i];
				if (singleBin != null && imagesLength[i] > 0) {
					obj[i] = new ByteArrayInputStream(singleBin);
				}
			}
		}
		return obj;
	}

	public void setImages(final byte[] images) {
		this.images = images;
	}

	@Override
	public Integer[] getReportLength() {
		return reportLength;
	}

	public void setReportLength(final Integer[] reportLength) {
		this.reportLength = reportLength;
	}

	@Override
	public Integer[] getImagesLength() {
		return imagesLength;
	}

	public void setImagesLength(final Integer[] imagesLength) {
		this.imagesLength = imagesLength;
	}

	@Override
	public String[] getImagesName() {
		return imagesName;
	}

	public void setImagesName(final String[] imagesNames) {
		this.imagesName = imagesNames;
	}

	public void setSubreportsNumber(final int subreportsNumber) {
		this.subreportsNumber = subreportsNumber;
	}

	@Override
	public int getSubreportsNumber() {
		return subreportsNumber;
	}
	
	public void setSubreportsName(final String[] subreportsName) {
		this.subreportsName = subreportsName;
	}

	@Override
	public String[] getSubreportsName() {
		return subreportsName;
	}

}
