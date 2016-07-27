package org.cmdbuild.workflow.widget;

import org.cmdbuild.model.widget.OpenNote;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.template.store.TemplateRepository;

public class OpenNoteWidgetFactory extends ValuePairWidgetFactory {

	public OpenNoteWidgetFactory(final TemplateRepository templateRespository, final Notifier notifier) {
		super(templateRespository, notifier);
	}

	@Override
	public String getWidgetName() {
		return "openNote";
	}

	@Override
	public Widget createWidget(final WidgetDefinition definition) {
		return new OpenNote();
	}
}
