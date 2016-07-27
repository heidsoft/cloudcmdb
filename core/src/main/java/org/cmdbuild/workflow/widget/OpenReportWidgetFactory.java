package org.cmdbuild.workflow.widget;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.model.widget.OpenReport;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.template.store.TemplateRepository;

import com.google.common.base.Splitter;

public class OpenReportWidgetFactory extends ValuePairWidgetFactory {

	private static final String WIDGET_NAME = "createReport";

	/*
	 * TODO: if a day we'll use multiple report types, use this now the only
	 * allowed type is "custom" and is managed client side
	 */
	private static final String REPORT_TYPE = "ReportType";
	public static final String REPORT_CODE = "ReportCode";
	public static final String FORCE_PDF = "ForcePDF";
	public static final String FORCE_CSV = "ForceCSV";
	/*
	 * TODO: use these when implementing save
	 */
	private static final String SAVE_TO_ALFRESCO = "StoreInAlfresco"; // hahahah
	public static final String STORE_IN_PROCESS = "StoreInProcess";
	public static final String READ_ONLY_ATTRIBUTES = "ReadOnlyAttributes";

	private static final String[] KNOWN_PARAMETERS = { BUTTON_LABEL, REPORT_TYPE, REPORT_CODE, SAVE_TO_ALFRESCO,
			STORE_IN_PROCESS, FORCE_CSV, FORCE_PDF, READ_ONLY_ATTRIBUTES };

	public OpenReportWidgetFactory(final TemplateRepository templateRespository, final Notifier notifier) {
		super(templateRespository, notifier);
	}

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	@Override
	protected Widget createWidget(final WidgetDefinition definition) {
		final String reportCode = readString(definition.get(REPORT_CODE));
		Validate.notEmpty(reportCode, REPORT_CODE + " is required");

		final OpenReport widget = new OpenReport();
		widget.setReportCode(reportCode);
		widget.setPreset(extractUnmanagedParameters(definition, KNOWN_PARAMETERS));
		forceFormat(definition, widget);
		widget.setReadOnlyAttributes(readOnlyAttributesOf(readString(definition.get(READ_ONLY_ATTRIBUTES))));

		return widget;
	}

	private void forceFormat(final Map<String, Object> valueMap, final OpenReport widget) {
		if (valueMap.containsKey(FORCE_PDF)) {
			widget.setForceFormat("pdf");
		} else if (valueMap.containsKey(FORCE_CSV)) {
			widget.setForceFormat("csv");
		}
	}

	private List<String> readOnlyAttributesOf(final String value) {
		final List<String> readOnlyAttributes;
		if (isEmpty(value)) {
			readOnlyAttributes = Collections.emptyList();
		} else {
			readOnlyAttributes = Splitter.on(",") //
					.omitEmptyStrings() //
					.trimResults() //
					.splitToList(defaultString(value));
		}
		return readOnlyAttributes;
	}

}
