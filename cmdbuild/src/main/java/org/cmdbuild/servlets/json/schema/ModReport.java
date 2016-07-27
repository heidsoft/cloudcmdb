package org.cmdbuild.servlets.json.schema;

import static org.cmdbuild.servlets.json.CommunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.FORMAT;
import static org.cmdbuild.servlets.json.CommunicationConstants.GROUPS;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.JRXML;
import static org.cmdbuild.servlets.json.CommunicationConstants.NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.REPORT_ID;
import static org.cmdbuild.utils.BinaryUtils.fromByte;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRQuery;
import net.sf.jasperreports.engine.JRSubreport;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignImage;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ReportException.ReportExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.report.ReportFactory;
import org.cmdbuild.report.ReportFactory.ReportExtension;
import org.cmdbuild.report.ReportFactory.ReportType;
import org.cmdbuild.report.ReportFactoryTemplateSchema;
import org.cmdbuild.report.ReportParameter;
import org.cmdbuild.services.store.report.Report;
import org.cmdbuild.services.store.report.ReportStore;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.utils.MethodParameterResolver;
import org.cmdbuild.servlets.utils.Parameter;
import org.cmdbuild.servlets.utils.Request;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ModReport extends JSONBaseWithSpringContext {

	private static class ReportImpl implements Report {

		private int id = 0;
		private String code = "";
		private String description = "";
		private String status = "A";
		private String user = "";
		private Date beginDate = new Date();

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

		public ReportImpl() {
			// TODO Auto-generated constructor stub
		}

		public ReportImpl(final Report existing) {
			setType(existing.getType());
			setId(existing.getId());
			setCode(existing.getCode());
			setDescription(existing.getDescription());
			setUser(existing.getUser());
			setQuery(existing.getQuery());
			setOriginalId(existing.getOriginalId());
			setJd(existing.getJd());
			setGroups(existing.getGroups());
			setRichReport(existing.getRichReportBA());
			setSimpleReport(existing.getSimpleReport());
			setWizard(existing.getWizard());
			setImages(existing.getImagesBA());
			setReportLength(existing.getReportLength());
			setImagesLength(existing.getImagesLength());
			setImagesName(existing.getImagesName());
			setSubreportsName(existing.getSubreportsName());
			setSubreportsNumber(existing.getSubreportsNumber());
		}

		/**
		 * id of the report we're editing, "-1" if it's a new one
		 * (administration side)
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

		public void setStatus(final String status) {
			this.status = status;
		}

		@Override
		public String getUser() {
			return user;
		}

		public void setUser(final String user) {
			this.user = user;
		}

		public void setBeginDate(final Date beginDate) {
			this.beginDate = beginDate;
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

	/**
	 * Print a report that lists all the classes
	 *
	 * @param format
	 * @throws Exception
	 */
	@JSONExported
	public void printSchema( //
			@Parameter(FORMAT) final String format //
	) throws Exception {
		final ReportFactoryTemplateSchema rfts = new ReportFactoryTemplateSchema( //
				dataSource(), //
				ReportExtension.valueOf(format.toUpperCase()), //
				cmdbuildConfiguration(), //
				userDataView(), //
				rootFilesStore() //
		);
		rfts.fillReport();
		sessionVars().setReportFactory(rfts);
	}

	/**
	 * Print a report with the detail of a class
	 *
	 * @param format
	 * @throws Exception
	 */
	@JSONExported
	public void printClassSchema(@Parameter(CLASS_NAME) final String className, @Parameter(FORMAT) final String format)
			throws Exception {

		final ReportFactoryTemplateSchema rfts = new ReportFactoryTemplateSchema( //
				dataSource(),//
				ReportExtension.valueOf(format.toUpperCase()), //
				className,//
				cmdbuildConfiguration(), //
				userDataView(), //
				rootFilesStore() //
		);

		rfts.fillReport();
		sessionVars().setReportFactory(rfts);
	}

	/**
	 *
	 * Is the first step of the report upload Analyzes the JRXML and eventually
	 * return the configuration of the second step
	 */

	@Admin
	@JSONExported
	public JSONObject analyzeJasperReport( //
			@Parameter(NAME) final String name, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(GROUPS) final String groups, //
			@Parameter(REPORT_ID) final int reportId, //
			@Parameter(value = JRXML, required = false) final FileItem file //
	) throws JSONException, NotFoundException {

		resetSession();
		final ReportImpl newReport = new ReportImpl();
		setReportSimpleAttributes(name, description, groups, reportId, newReport);

		final JSONObject out = new JSONObject();
		if (file.getSize() > 0) {
			setReportImagesAndSubReports(out, file, newReport);
		} else {
			// there is no second step
			out.put("skipSecondStep", true);
		}

		sessionVars().setNewReport(newReport);
		final Report test = sessionVars().getNewReport();
		return out;
	}

	private void setReportImagesAndSubReports(final JSONObject serializer, final FileItem file,
			final ReportImpl newReport) throws JSONException {
		String[] imagesNames = null;
		String[] subreportsNames = null;

		final JasperDesign jd = loadJasperDesign(file);
		checkJasperDesignParameters(jd);
		final List<JRDesignImage> designImages = ReportFactory.getImages(jd);

		if (ReportFactory.checkDuplicateImages(designImages)) { // check
																// duplicates
			serializer.put("duplicateimages", true);
			serializer.put("images", "");
			serializer.put("subreports", "");
		} else {
			imagesNames = manageImages(serializer, designImages);
			subreportsNames = manageSubReports(serializer, jd);
		}

		newReport.setImagesName(imagesNames);
		newReport.setSubreportsNumber(subreportsNames.length);
		newReport.setSubreportsName(subreportsNames);
		newReport.setJd(jd);
	}

	private void setReportSimpleAttributes(final String name, final String description, final String groups,
			final int reportId, final ReportImpl newReport) {
		newReport.setOriginalId(reportId);
		newReport.setCode(name);
		newReport.setDescription(description);
		newReport.setGroups(parseSelectedGroup(groups));
	}

	private String[] manageSubReports(final JSONObject serializer, final JasperDesign jd) throws JSONException {
		JSONArray jsonArray;
		JSONObject jsonObject;
		int subreportsNumber = 0;
		final List<JRSubreport> subreports = ReportFactory.getSubreports(jd);
		final String[] subreportsName = new String[subreports.size()];
		jsonArray = new JSONArray();
		for (final JRSubreport subreport : subreports) {
			final String subreportName = ReportFactory.getSubreportName(subreport);
			subreportsName[subreportsNumber] = subreportName;
			subreportsNumber++;

			// client
			jsonObject = new JSONObject();
			jsonObject.put("name", subreportName);
			jsonArray.put(jsonObject);
		}
		serializer.put("subreports", jsonArray);
		ReportFactory.prepareDesignSubreportsForUpload(subreports); // update
																	// expressions
																	// in design
		return subreportsName;
	}

	private String[] manageImages(final JSONObject serializer, final List<JRDesignImage> designImages)
			throws JSONException {
		JSONArray jsonArray;
		JSONObject jsonObject;
		String[] imagesNames;
		jsonArray = new JSONArray();
		imagesNames = new String[designImages.size()];
		for (int i = 0; i < designImages.size(); i++) {
			final String imageFilename = ReportFactory.getImageFileName(designImages.get(i));
			imagesNames[i] = imageFilename;

			// client
			jsonObject = new JSONObject();
			jsonObject.put("name", imageFilename);
			jsonArray.put(jsonObject);
		}
		serializer.put("images", jsonArray);
		ReportFactory.prepareDesignImagesForUpload(designImages); // update
																	// expressions
																	// in design
		return imagesNames;
	}

	private String[] parseSelectedGroup(final String groups) {
		final String[] stringGroups;
		if (groups != null && !groups.equals("")) {
			stringGroups = groups.split(",");
		} else {
			stringGroups = new String[0];
		}

		return stringGroups;
	}

	private void checkJasperDesignParameters(final JasperDesign jd) {
		final JRParameter[] parameters = jd.getParameters();
		for (final JRParameter parameter : parameters) {
			ReportParameter.parseJrParameter(parameter);
		}
	}

	private JasperDesign loadJasperDesign(final FileItem file) {
		JasperDesign jd = null;
		try {
			jd = JRXmlLoader.load(file.getInputStream());
		} catch (final Exception e) {
			Log.REPORT.error("Error loading report", e);
			throw ReportExceptionType.REPORT_INVALID_FILE.createException();
		}
		return jd;
	}

	@Admin
	@JSONExported
	/**
	 * Is the second step of the report
	 * import. Manage the sub reports and
	 * the images
	 *
	 * @param files
	 * @throws JSONException
	 * @throws AuthException
	 */
	public void importJasperReport(@Request(MethodParameterResolver.MultipartRequest) final List<FileItem> files)
			throws JSONException, AuthException {
		final ReportImpl newReport = new ReportImpl(sessionVars().getNewReport());

		if (newReport.getJd() != null) {
			importSubreportsAndImages(files, newReport);
		}

		saveReport(newReport);
		resetSession();
	}

	@Admin
	@JSONExported
	public void saveJasperReport() {
		final Report newReport = sessionVars().getNewReport();
		saveReport(newReport);
	}

	private void saveReport(final Report newReport) {
		final ReportStore reportStore = reportStore();
		try {
			if (newReport.getOriginalId() < 0) {
				reportStore.insertReport(newReport);
			} else {
				reportStore.updateReport(newReport);
			}
		} catch (final SQLException e) {
			Log.REPORT.error("Error saving report");
		} catch (final IOException e) {
			Log.REPORT.error("Error saving report");
			e.printStackTrace();
		}
	}

	private void importSubreportsAndImages(final List<FileItem> files, final ReportImpl newReport) {
		try {
			// get IMAGES
			final int nImages = newReport.getImagesName().length;

			// imageByte contains the stream of imagesFiles[]
			final byte[][] imageByte = new byte[nImages][];
			// lengthImageByte contains the lengths of all imageByte[]
			final Integer lengthImagesByte[] = new Integer[nImages];

			{
				int i = 0;
				for (final String imageName : newReport.getImagesName()) {
					imageByte[i] = null;
					for (final FileItem file : files) {
						if (file.getName().equals(imageName) || file.getFieldName().equals(imageName)) {
							imageByte[i] = file.get();
							break;
						}
					}
					if (imageByte[i] == null) {
						throw ReportExceptionType.REPORT_UPLOAD_ERROR.createException("Expected image '" + imageName
								+ "' not found in uploaded data");
					}
					i++;
				}
			}

			// get REPORTS
			final int nReports = newReport.getSubreportsNumber() + 1; // subreports
																		// + 1
																		// master
																		// report

			// imageByte contains the stream of imagesFiles[]
			final byte[][] reportByte = new byte[nReports][];
			// lengthImageByte contains the lengths of all imageByte[]
			final Integer lengthReportByte[] = new Integer[nReports];

			for (int i = 0; i < nReports - 1; i++) {
				// load the subreport .jasper file and put it in reportByte
				reportByte[i + 1] = null;
				final String subreportName = newReport.getSubreportsName()[i];
				for (final FileItem file : files) {
					if (file.getName().equals(subreportName) || file.getFieldName().equals(subreportName)) {
						// i+1 because of the master report with index 0
						reportByte[i + 1] = file.get();
						break;
					}
				}
				if (reportByte[i + 1] == null) {
					throw ReportExceptionType.REPORT_UPLOAD_ERROR.createException("Expected jasper report '"
							+ subreportName + "' not found in uploaded data");
				}
			}

			// check if all files have been uploaded
			boolean fileNotUploaded = false;

			for (int i = 0; i < nImages; i++) {
				if (imageByte[i] == null) {
					fileNotUploaded = true;
				}
			}

			for (int i = 1; i < nReports; i++) { // must start at 1 because 0 is
													// master report
				if (reportByte[i] == null) {
					fileNotUploaded = true;
				}
			}

			if (!fileNotUploaded) {

				// IMAGES
				for (int i = 0; i < nImages; i++) {
					lengthImagesByte[i] = imageByte[i].length;
				}

				int totByte = 0; // total n. of bytes needed to store all images
				for (int i = 0; i < nImages; i++) {
					totByte += lengthImagesByte[i];
				}

				// array of bytes to store into db all reports
				final byte[] imagesByte = new byte[totByte];

				int startAt = 0; // determinate position in which starts a new
									// image

				// puts in imageByte all the reports
				for (int i = 0; i < nImages; i++) {
					for (int j = 0; j < lengthImagesByte[i]; j++) {
						imagesByte[startAt + j] = imageByte[i][j];
					}
					startAt += lengthImagesByte[i];
				}

				// REPORTS
				final ByteArrayOutputStream os = new ByteArrayOutputStream();
				JasperCompileManager.compileReportToStream(newReport.getJd(), os);
				reportByte[0] = os.toByteArray(); // master report in bytes

				for (int i = 0; i < nReports; i++) {
					lengthReportByte[i] = reportByte[i].length;
				}

				totByte = 0; // total n. of bytes needed to store all reports
								// (master and subreports)
				for (int i = 0; i < nReports; i++) {
					totByte += lengthReportByte[i];
				}

				// array of bytes to store into db
				final byte[] reportsByte = new byte[totByte];

				startAt = 0; // determinate position in which starts a new
								// report

				// puts in reportByte all the reports
				for (int i = 0; i < nReports; i++) {
					for (int j = 0; j < lengthReportByte[i]; j++) {
						reportsByte[startAt + j] = reportByte[i][j];
					}
					startAt += lengthReportByte[i];
				}

				// update report data
				newReport.setType(ReportType.CUSTOM);
				newReport.setStatus("A");
				newReport.setRichReport(reportsByte);
				newReport.setSimpleReport(reportsByte);
				newReport.setReportLength(lengthReportByte);
				newReport.setBeginDate(new Date());

				// update query
				final JRQuery jrQuery = newReport.getJd().getQuery();
				if (jrQuery != null) {
					final String query = jrQuery.getText();
					query.replaceAll("\"", "\\\"");
					newReport.setQuery(query);
				}

				if (imageByte != null) {
					newReport.setImages(imagesByte);
					newReport.setImagesLength(lengthImagesByte);
				}
			} else {
				throw ReportExceptionType.REPORT_UPLOAD_ERROR.createException();
			}
		} catch (final JRException e) {
			Log.REPORT.error("Error compiling report", e);
			throw ReportExceptionType.REPORT_COMPILE_ERROR.createException();
		} catch (final NoClassDefFoundError e) {
			Log.REPORT.error("Class not found error", e);
			throw ReportExceptionType.REPORT_NOCLASS_ERROR.createException(e.getMessage());
		}
	}

	@JSONExported
	public void deleteReport(@Parameter(ID) final int id) throws JSONException {
		reportStore().deleteReport(id);
	}

	/**
	 * Reset session, last "import report" operation
	 *
	 * @param serializer
	 * @return
	 * @throws JSONException
	 */
	@JSONExported
	public void resetSession() throws JSONException {
		sessionVars().removeNewReport();
	}
}
