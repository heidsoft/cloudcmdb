package org.cmdbuild.workflow.widget;

import org.cmdbuild.model.widget.Calendar;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.template.store.TemplateRepository;

public class CalendarWidgetFactory extends ValuePairWidgetFactory {

	private static final String WIDGET_NAME = "calendar";

	public static final String TARGET_CLASS = "ClassName";
	public static final String EVENT_CLASS = "EventClass";
	public static final String CQL_FILTER = "Filter";
	public static final String TITLE = "EventTitle";
	public static final String START_DATE = "EventStartDate";
	public static final String END_DATE = "EventEndDate";
	public static final String DEFAULT_DATE = "DefaultDate";

	public CalendarWidgetFactory(final TemplateRepository templateRespository, final Notifier notifier) {
		super(templateRespository, notifier);
	}

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	@Override
	protected Widget createWidget(final WidgetDefinition definition) {
		final Calendar widget = new Calendar();

		final String filter = readString(definition.get(CQL_FILTER));
		if (filter != null) {
			widget.setFilter(filter);
			widget.setEventClass(readClassNameFromCQLFilter(filter));
		} else {
			widget.setEventClass(readString(definition.get(EVENT_CLASS)));
		}

		widget.setEventTitle(readString(definition.get(TITLE)));
		widget.setStartDate(readString(definition.get(START_DATE)));
		widget.setEndDate(readString(definition.get(END_DATE)));
		widget.setDefaultDate(readString(definition.get(DEFAULT_DATE)));

		return widget;
	}

}
