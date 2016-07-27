package org.cmdbuild.servlets.json;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Multimaps.index;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.services.json.dto.JsonResponse.success;
import static org.cmdbuild.servlets.json.CommunicationConstants.ACTIVE_ONLY;
import static org.cmdbuild.servlets.json.CommunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.CLASS_NAMES;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.PARAMS;
import static org.cmdbuild.servlets.json.CommunicationConstants.WIDGET;
import static org.cmdbuild.servlets.json.CommunicationConstants.WIDGET_ID;
import static org.cmdbuild.servlets.json.schema.Utils.toIterable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.utils.Parameter;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMActivityInstance;
import org.cmdbuild.workflow.CMActivityWidget;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.base.Function;

public class Widget extends JSONBaseWithSpringContext {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final Function<org.cmdbuild.model.widget.Widget, String> SOURCE_CLASS = new Function<org.cmdbuild.model.widget.Widget, String>() {

		@Override
		public String apply(final org.cmdbuild.model.widget.Widget input) {
			return input.getSourceClass();
		}

	};

	@JSONExported
	public JsonResponse callWidget( //
			@Parameter(ID) final Long cardId, //
			@Parameter(CLASS_NAME) final String className, //
			@Parameter(required = false, value = "activityId") final String activityInstanceId, //
			@Parameter(WIDGET_ID) final String widgetId, //
			@Parameter(required = false, value = "action") final String action, //
			@Parameter(required = false, value = PARAMS) final String jsonParams //
	) throws Exception {
		final boolean isActivity = activityInstanceId != null;
		if (isActivity) {
			return callProcessWidget(cardId, className, activityInstanceId, widgetId, action, jsonParams);
		} else {
			return callCardWidget(cardId, className, Long.parseLong(widgetId), action, jsonParams);
		}
	}

	private JsonResponse callCardWidget(final Long cardId, final String className, final Long widgetId,
			final String action, final String jsonParams) throws Exception {
		final org.cmdbuild.model.widget.Widget widgetToExecute = widgetLogic().getWidget(widgetId);
		final Card card = systemDataAccessLogic().fetchCard(className, cardId);
		final Map<String, Object> params = readParams(jsonParams);
		final Map<String, Object> attributesNameToValue = newHashMap();
		for (final Entry<String, Object> entry : card.getAttributes().entrySet()) {
			attributesNameToValue.put(entry.getKey(), entry.getValue());
		}
		return success(widgetToExecute.executeAction(action, params, attributesNameToValue));
	}

	private JsonResponse callProcessWidget(final Long processCardId, final String className,
			final String activityInstanceId, final String widgetId, final String action, final String jsonParams)
			throws Exception {

		final Map<String, Object> params = readParams(jsonParams);
		Object response = null;
		final WorkflowLogic logic = workflowLogic();
		final List<CMActivityWidget> widgets;
		if (processCardId > 0) {
			final CMActivityInstance activityInstance = logic.getActivityInstance(className, processCardId,
					activityInstanceId);
			widgets = activityInstance.getWidgets();
		} else {
			// For a new process, there isn't activity instances. So retrieve
			// the start activity
			// and look for them widgets
			final CMActivity activity = logic.getStartActivity(className);
			widgets = activity.getWidgets();
		}

		for (final CMActivityWidget widget : widgets) {
			if (widget.getStringId().equals(widgetId)) {
				/*
				 * TODO
				 * 
				 * I don't know WTF pass instead of null, something for the
				 * server side TemplateResolver
				 */
				response = widget.executeAction(action, params, null);
			}
		}

		return success(response);
	}

	private Map<String, Object> readParams(final String jsonParams) throws IOException, JsonParseException,
			JsonMappingException {
		final Map<String, Object> params;

		if (jsonParams == null) {
			params = new HashMap<String, Object>();
		} else {
			params = OBJECT_MAPPER.readValue(jsonParams,
					OBJECT_MAPPER.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class));
		}
		return params;
	}

	@Admin
	@JSONExported
	public JsonResponse create( //
			@Parameter(CLASS_NAME) final String className, //
			@Parameter(value = WIDGET, required = true) final String jsonWidget //
	) throws Exception {
		final org.cmdbuild.model.widget.Widget widget = OBJECT_MAPPER.readValue(jsonWidget,
				org.cmdbuild.model.widget.Widget.class);
		widget.setSourceClass(className);
		final org.cmdbuild.model.widget.Widget response = widgetLogic().createWidget(widget);
		// FIXME return only the id
		return success(response);
	}

	@JSONExported
	public JsonResponse readAll( // .asMap()
			@Parameter(value = ACTIVE_ONLY, required = false) final boolean activeOnly, //
			@Parameter(value = CLASS_NAMES, required = false) final String jsonClassNames //
	) {
		final Iterable<String> classNames;
		if (isBlank(jsonClassNames)) {
			classNames = emptyList();
		} else {
			classNames = toIterable(jsonClassNames);
		}
		return success(index(widgetLogic().getAllWidgets(activeOnly, classNames), SOURCE_CLASS).asMap());
	}

	@JSONExported
	public JsonResponse readAllForClass( //
			@Parameter(value = ACTIVE_ONLY, required = false) final boolean activeOnly, //
			@Parameter(value = CLASS_NAME) final String className //
	) {
		return success(from(widgetLogic().getAllWidgets(activeOnly, asList(className))).toList());
	}

	@JSONExported
	public JsonResponse read( //
			@Parameter(CLASS_NAME) final String className, //
			@Parameter(ID) final Long widgetId //
	) throws Exception {
		return success(widgetLogic().getWidget(widgetId));
	}

	@Admin
	@JSONExported
	public JsonResponse update( //
			@Parameter(CLASS_NAME) final String className, //
			@Parameter(value = WIDGET, required = true) final String jsonWidget //
	) throws Exception {
		final org.cmdbuild.model.widget.Widget widget = OBJECT_MAPPER.readValue(jsonWidget,
				org.cmdbuild.model.widget.Widget.class);
		widget.setSourceClass(className);
		widgetLogic().updateWidget(widget);
		return success();
	}

	@Admin
	@JSONExported
	public void delete( //
			@Parameter(CLASS_NAME) final String className, //
			@Parameter(ID) final Long widgetId //
	) throws Exception {
		widgetLogic().deleteWidget(widgetId);
	}

}
