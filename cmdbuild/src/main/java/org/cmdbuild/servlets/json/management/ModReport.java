package org.cmdbuild.servlets.json.management;

import static com.google.common.collect.Lists.newArrayList;
import static org.cmdbuild.dao.query.clause.Clauses.call;
import static org.cmdbuild.logic.report.Predicates.currentGroupAllowed;
import static org.cmdbuild.servlets.json.CommunicationConstants.ATTRIBUTES;
import static org.cmdbuild.servlets.json.CommunicationConstants.CARD_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.CODE;
import static org.cmdbuild.servlets.json.CommunicationConstants.EXTENSION;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILTER;
import static org.cmdbuild.servlets.json.CommunicationConstants.FORMAT;
import static org.cmdbuild.servlets.json.CommunicationConstants.FUNCTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.LIMIT;
import static org.cmdbuild.servlets.json.CommunicationConstants.SORT;
import static org.cmdbuild.servlets.json.CommunicationConstants.START;
import static org.cmdbuild.servlets.json.CommunicationConstants.STATE;
import static org.cmdbuild.servlets.json.CommunicationConstants.TYPE;
import static org.cmdbuild.servlets.json.schema.Utils.toIterable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.cmdbuild.common.utils.TempDataSource;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.exception.ReportException.ReportExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.QueryOptions.QueryOptionsBuilder;
import org.cmdbuild.logic.mapping.json.JsonFilterHelper;
import org.cmdbuild.report.CustomProperties;
import org.cmdbuild.report.ReportFactory;
import org.cmdbuild.report.ReportFactory.ReportExtension;
import org.cmdbuild.report.ReportFactory.ReportType;
import org.cmdbuild.report.ReportFactoryDB;
import org.cmdbuild.report.ReportFactoryTemplate;
import org.cmdbuild.report.ReportFactoryTemplateDetail;
import org.cmdbuild.report.ReportFactoryTemplateList;
import org.cmdbuild.report.ReportParameter;
import org.cmdbuild.report.ReportParameterConverter;
import org.cmdbuild.services.store.report.Report;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.serializers.AttributeSerializer;
import org.cmdbuild.servlets.json.serializers.ReportSerializer;
import org.cmdbuild.servlets.json.util.FlowStatusFilterElementGetter;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ModReport extends JSONBaseWithSpringContext {

	@JSONExported
	public JSONArray getReportTypesTree(final Map<String, String> params) throws JSONException {
		final JSONArray rows = new JSONArray();
		for (final String type : reportStore().getReportTypes()) {
			final JSONObject jsonObj = new JSONObject();
			jsonObj.put("id", type);
			jsonObj.put("text", type);
			jsonObj.put("type", "report");
			jsonObj.put("leaf", true);
			jsonObj.put("cls", "file");
			jsonObj.put("selectable", true);
			rows.put(jsonObj);
		}

		return rows;
	}

	@JSONExported
	public JSONObject getReportsByType( //
			@Parameter(TYPE) final String reportType, //
			@Parameter(LIMIT) final int limit, @Parameter(START) final int offset) throws JSONException {

		final JSONArray rows = new JSONArray();
		int numRecords = 0;
		for (final Report report : reportStore().findReportsByType(ReportType.valueOf(reportType.toUpperCase()))) {
			if (currentGroupAllowed(userStore()).apply(report)) {
				++numRecords;
				if (numRecords > offset && numRecords <= offset + limit) {
					rows.put(new ReportSerializer().toClient(report));
				}
			}
		}

		final JSONObject out = new JSONObject();
		out.put("rows", rows);
		out.put("results", numRecords);
		return out;
	}

	@JSONExported
	public JSONObject createReportFactoryByTypeCode( //
			@Parameter(TYPE) final String type, //
			@Parameter(CODE) final String code //
	) throws Exception {

		final Report report = reportStore().findReportByTypeAndCode(ReportType.valueOf(type.toUpperCase()), code);

		if (report == null) {
			throw ReportExceptionType.REPORT_NOTFOUND.createException(code);
		}

		if (!currentGroupAllowed(userStore()).apply(report)) {
			throw ReportExceptionType.REPORT_GROUPNOTALLOWED.createException(report.getCode());
		}

		final JSONObject out = new JSONObject();
		ReportFactoryDB factory = null;
		if (type.equalsIgnoreCase(ReportType.CUSTOM.toString())) {
			factory = new ReportFactoryDB(dataSource(), cmdbuildConfiguration(), reportStore(), report.getId(), null);
			boolean filled = false;
			if (factory.getReportParameters().isEmpty()) {
				factory.fillReport();
				filled = true;
			} else {
				for (final JSONObject element : serializeParameters(factory)) {
					out.append("attribute", element);
				}
			}
			out.put("filled", filled);
		}
		sessionVars().setReportFactory(factory);
		return out;
	}

	/**
	 * Create report factory obj
	 */
	@JSONExported
	public JSONObject createReportFactory( //
			@Parameter(TYPE) final String type, //
			@Parameter(ID) final int id, //
			@Parameter(EXTENSION) final String extension //
	) throws Exception { //

		ReportFactoryDB reportFactory = null;

		final JSONObject out = new JSONObject();
		if (ReportType.valueOf(type.toUpperCase()) == ReportType.CUSTOM) {
			final ReportExtension reportExtension = ReportExtension.valueOf(extension.toUpperCase());
			reportFactory = new ReportFactoryDB(dataSource(), cmdbuildConfiguration(), reportStore(), id,
					reportExtension);

			// if zip extension, do not compile
			if (reportExtension == ReportExtension.ZIP) {
				out.put("filled", true);
			}

			else {
				// if no parameters
				if (reportFactory.getReportParameters().isEmpty()) {
					reportFactory.fillReport();
					out.put("filled", true);
				}

				// else, prepare required parameters
				else {
					out.put("filled", false);
					for (final JSONObject element : serializeParameters(reportFactory)) {
						out.append("attribute", element);
					}
				}
			}
		}

		sessionVars().setReportFactory(reportFactory);
		return out;
	}

	private Iterable<JSONObject> serializeParameters(final ReportFactoryDB reportFactory)
			throws ClassNotFoundException, IOException, JSONException {
		final Collection<JSONObject> output = newArrayList();
		for (final ReportParameter reportParameter : reportFactory.getReportParameters()) {
			final CMAttribute attribute = ReportParameterConverter.of(reportParameter).toCMAttribute();
			final Map<String, String> metadata = new CustomProperties(reportParameter.getJrParameter()
					.getPropertiesMap()).getFilterParameters();
			output.add(AttributeSerializer.newInstance() //
					.withDataView(systemDataView()) //
					.build()//
					.toClient(attribute, metadata));
		}
		return output;
	}

	/**
	 * Set user-defined parameters and fill report
	 *
	 * @throws Exception
	 */
	@JSONExported
	public void updateReportFactoryParams( //
			final Map<String, String> formParameters //
	) throws Exception {

		final ReportFactoryDB reportFactory = (ReportFactoryDB) sessionVars().getReportFactory();
		if (formParameters.containsKey("reportExtension")) {
			reportFactory.setReportExtension(ReportExtension.valueOf(formParameters.get("reportExtension")
					.toUpperCase()));
		}

		for (final ReportParameter reportParameter : reportFactory.getReportParameters()) {
			// update parameter
			reportParameter.parseValue(formParameters.get(reportParameter.getFullName()));
			Log.REPORT.debug("Setting parameter " + reportParameter.getFullName() + ": " + reportParameter.getValue());
		}

		reportFactory.fillReport();
		sessionVars().setReportFactory(reportFactory);
	}

	/**
	 * Print report to output stream
	 *
	 * @param noDelete
	 *            this may be requested for wf server side processing
	 */
	@JSONExported
	public DataHandler printReportFactory( //
			@Parameter(value = "donotdelete", required = false) final boolean notDelete //
	) throws Exception {

		final ReportFactory reportFactory = sessionVars().getReportFactory();
		// TODO: report filename should be always read from jasperPrint obj
		// get report filename
		String filename = "";
		if (reportFactory instanceof ReportFactoryDB) {
			final ReportFactoryDB reportFactoryDB = (ReportFactoryDB) reportFactory;
			filename = reportFactoryDB.getReportCard().getCode().replaceAll(" ", "");
		} else if (reportFactory instanceof ReportFactoryTemplate) {
			final ReportFactoryTemplate reportFactoryTemplate = (ReportFactoryTemplate) reportFactory;
			filename = reportFactoryTemplate.getJasperPrint().getName();
		}

		// add extension
		filename += "." + reportFactory.getReportExtension().toString().toLowerCase();

		// send to stream
		final DataSource dataSource = TempDataSource.newInstance() //
				.withName(filename) //
				.withContentType(reportFactory.getContentType()) //
				.build();
		final OutputStream outputStream = dataSource.getOutputStream();
		reportFactory.sendReportToStream(outputStream);
		outputStream.flush();
		outputStream.close();

		if (!notDelete) {
			sessionVars().removeReportFactory();
		}

		return new DataHandler(dataSource);
	}

	/**
	 * Print cards on screen
	 */
	@JSONExported
	public void printCurrentView( //
			@Parameter(TYPE) final String type, //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(LIMIT) final int limit, //
			@Parameter(START) final int offset, //
			@Parameter(value = FILTER, required = false) final JSONObject filter, //
			@Parameter(value = SORT, required = false) final JSONArray sorters, //
			@Parameter(ATTRIBUTES) final JSONArray attributes, //
			// for processes only
			@Parameter(value = STATE, required = false) final String flowStatus //
	) throws Exception {
		sessionVars().removeReportFactory();
		final QueryOptionsBuilder queryOptionsBuilder = QueryOptions.newQueryOption() //
				.limit(limit) //
				.offset(offset) //
				.orderBy(sorters);
		if (flowStatus != null) {
			queryOptionsBuilder.filter(new JsonFilterHelper(filter) //
					.merge(new FlowStatusFilterElementGetter(lookupHelper(), flowStatus)));
		} else {
			queryOptionsBuilder.filter(filter);
		}
		final QueryOptions queryOptions = queryOptionsBuilder.build();
		final ReportFactoryTemplateList rft = new ReportFactoryTemplateList( //
				dataSource(), //
				ReportExtension.valueOf(type.toUpperCase()), //
				queryOptions, //
				toIterable(attributes), //
				userDataView().findClass(className), //
				userDataView(), //
				rootFilesStore(), //
				cmdbuildConfiguration());
		rft.fillReport();
		sessionVars().setReportFactory(rft);
	}

	@JSONExported
	public void printSqlView( //
			@Parameter(TYPE) final String type, //
			@Parameter(FUNCTION) final String function, //
			@Parameter(ATTRIBUTES) final JSONArray attributes, //
			@Parameter(LIMIT) final int limit, //
			@Parameter(START) final int offset, //
			@Parameter(value = FILTER, required = false) final JSONObject filter, //
			@Parameter(value = SORT, required = false) final JSONArray sorters //
	) throws Exception {
		sessionVars().removeReportFactory();
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.limit(limit) //
				.offset(offset) //
				.orderBy(sorters) //
				.filter(filter) //
				.build();
		final ReportFactoryTemplateList rft = new ReportFactoryTemplateList( //
				dataSource(), ReportExtension.valueOf(type.toUpperCase()), //
				queryOptions, //
				toIterable(attributes), //
				call(userDataView().findFunctionByName(function)), //
				userDataView(), //
				rootFilesStore(), //
				cmdbuildConfiguration());
		rft.fillReport();
		sessionVars().setReportFactory(rft);
	}

	@JSONExported
	public void printCardDetails( //
			@Parameter(FORMAT) final String format, //
			@Parameter(CLASS_NAME) final String className, //
			@Parameter(CARD_ID) final Long cardId) throws Exception {
		final ReportFactoryTemplateDetail rftd = new ReportFactoryTemplateDetail(//
				dataSource(), //
				className, //
				cardId, //
				ReportExtension.valueOf(format.toUpperCase()), //
				userDataView(), //
				rootFilesStore(), //
				userDataAccessLogic(), //
				localization(), //
				cmdbuildConfiguration());
		rftd.fillReport();
		sessionVars().setReportFactory(rftd);
	}

}
