package org.cmdbuild.workflow.widget;

import org.cmdbuild.model.widget.WebService;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.template.store.TemplateRepository;

public class WebServiceWidgetFactory extends ValuePairWidgetFactory {

	/*
	 * **************************************************
	 * Follows the list of key/value pairs that define the configuration of a
	 * Web Service Widget **************************************************
	 * 
	 * ButtonLabel="" EndPoint="" Method="doSomthing" NameSpacePrefix=""
	 * NameSpaceURI="" NodesToUseAsRows="" NodesToUseAsColumns=""
	 * SingleSelect="true" Mandatory="false", ReadOnly="false"
	 * 
	 * some request parameters like param1="cql"
	 * 
	 * OutputVariableName as String[]
	 */

	private static final String WIDGET_NAME = "webService";

	private static final String ENDPOINT = "EndPoint";
	private static final String METHOD = "Method";
	private static final String NS_PREFIX = "NameSpacePrefix";
	private static final String NS_URI = "NameSpaceURI";
	private static final String NODES_TO_USE_AS_ROWS = "NodesToUseAsRows";
	private static final String NODES_TO_USE_AS_COLUMNS = "NodesToUseAsColumns";
	private static final String MANDATORY = "Mandatory";
	private static final String SINGLE_SELECT = "SingleSelect";
	private static final String READ_ONLY = "ReadOnly";
	private static final String OUTPUT_SEPARATOR = "OutputSeparator";

	public WebServiceWidgetFactory(final TemplateRepository templateRespository, final Notifier notifier) {
		super(templateRespository, notifier);
	}

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	@Override
	protected Widget createWidget(final WidgetDefinition definition) {
		final WebService webService = new WebService();
		webService.setEndPoint(readString(definition.get(ENDPOINT)));
		webService.setMethod(readString(definition.get(METHOD)));
		webService.setNameSpacePrefix(readString(definition.get(NS_PREFIX)));
		webService.setNameSpaceURI(readString(definition.get(NS_URI)));
		webService.setNodesToUseAsRows(readCommaSeparatedString(definition.get(NODES_TO_USE_AS_ROWS)));
		webService.setNodesToUseAsColumns(readCommaSeparatedString(definition.get(NODES_TO_USE_AS_COLUMNS)));
		webService.setMandatory(readBooleanFalseIfMissing(definition.get(MANDATORY)));
		webService.setSingleSelect(readBooleanFalseIfMissing(definition.get(SINGLE_SELECT)));
		webService.setReadOnly(readBooleanFalseIfMissing(definition.get(READ_ONLY)));
		webService.setOutputSeparator(readString(definition.get(OUTPUT_SEPARATOR)));

		webService.setOutputName(readString(definition.get(OUTPUT_KEY)));
		webService.setCallParameters(extractUnmanagedStringParameters(definition, //
				BUTTON_LABEL, //
				ENDPOINT, //
				METHOD, //
				NS_PREFIX, //
				NS_URI, //
				NODES_TO_USE_AS_ROWS, //
				NODES_TO_USE_AS_COLUMNS, //
				MANDATORY, //
				SINGLE_SELECT, //
				READ_ONLY, //
				OUTPUT_SEPARATOR //
				) //
				);

		return webService;
	}

}
