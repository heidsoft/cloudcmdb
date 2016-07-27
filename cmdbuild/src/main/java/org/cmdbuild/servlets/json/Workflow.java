package org.cmdbuild.servlets.json;

import static org.cmdbuild.servlets.json.CommunicationConstants.*;
import static org.cmdbuild.servlets.json.CommunicationConstants.ADVANCE;
import static org.cmdbuild.servlets.json.CommunicationConstants.ATTRIBUTES;
import static org.cmdbuild.servlets.json.CommunicationConstants.BEGIN_DATE;
import static org.cmdbuild.servlets.json.CommunicationConstants.CARD_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.CLASS_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILTER;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID_CLASS;
import static org.cmdbuild.servlets.json.CommunicationConstants.LIMIT;
import static org.cmdbuild.servlets.json.CommunicationConstants.PROCESS_INSTANCE_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.SORT;
import static org.cmdbuild.servlets.json.CommunicationConstants.START;
import static org.cmdbuild.servlets.json.CommunicationConstants.STATE;
import static org.cmdbuild.servlets.json.CommunicationConstants.VERSION;
import static org.cmdbuild.servlets.json.CommunicationConstants.WW;
import static org.cmdbuild.servlets.json.CommunicationConstants.XPDL;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.common.Constants;
import org.cmdbuild.common.template.TemplateResolver;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.exception.ConsistencyException.ConsistencyExceptionType;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.mapping.json.JsonFilterHelper;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.serializers.JsonWorkflowDTOs.JsonActivityDefinition;
import org.cmdbuild.servlets.json.serializers.JsonWorkflowDTOs.JsonActivityInstance;
import org.cmdbuild.servlets.json.serializers.JsonWorkflowDTOs.JsonProcessCard;
import org.cmdbuild.servlets.json.util.FlowStatusFilterElementGetter;
import org.cmdbuild.servlets.utils.Parameter;
import org.cmdbuild.workflow.ActivityPerformer;
import org.cmdbuild.workflow.ActivityPerformerExpressionEvaluator;
import org.cmdbuild.workflow.BshActivityPerformerExpressionEvaluator;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMProcessInstance;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.user.UserActivityInstance;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

public class Workflow extends JSONBaseWithSpringContext {

	/**
	 * Get the workItems OR closed processes, depending on the state required.
	 * If required state is closed, then processes with state closed.*
	 * (completed/terminated/aborted) will be returned. If state is open, the
	 * activities in open.not_running.not_started and open.running will be
	 * returned
	 *
	 * @param params
	 * @return
	 * @throws JSONException
	 * @throws CMWorkflowException
	 */
	// TODO: but is the right name? It returns ProcessInstances
	@JSONExported
	@SuppressWarnings("serial")
	public JsonResponse getProcessInstanceList( //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = FILTER, required = false) final JSONObject filter, //
			@Parameter(LIMIT) final int limit, //
			@Parameter(START) final int offset, //
			@Parameter(value = SORT, required = false) final JSONArray sorters, //
			@Parameter(STATE) final String flowStatus //
	) throws JSONException, CMWorkflowException {
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.limit(limit) //
				.offset(offset) //
				.orderBy(sorters) //
				.filter(new JsonFilterHelper(filter) //
						.merge(new FlowStatusFilterElementGetter(lookupHelper(), flowStatus))) //
				.build();

		final List<JsonProcessCard> processInstances = Lists.newArrayList();
		final PagedElements<UserProcessInstance> response = workflowLogic().query(className, queryOptions);
		for (final UserProcessInstance pi : response) {
			processInstances.add(new JsonProcessCard(pi, translationFacade(), lookupSerializer()));
		}

		return JsonResponse.success(new HashMap<String, Object>() {
			{
				put("results", response.totalSize());
				put("rows", processInstances);
			}
		});

	}

	@JSONExported
	public JsonResponse getStartActivity( //
			@Parameter(CLASS_ID) final Long processClassId) throws CMWorkflowException {
		final CMActivity activityDefinition = workflowLogic().getStartActivityOrDie(processClassId);

		return JsonResponse.success(new JsonActivityDefinition( //
				activityDefinition, //
				performerFor(activityDefinition)));
	}

	private String performerFor(final CMActivity activityDefinition) {
		final String performerName;
		final ActivityPerformer performer = activityDefinition.getFirstNonAdminPerformer();
		switch (performer.getType()) {
		case ROLE: {
			performerName = performer.getValue();
			break;
		}
		case EXPRESSION: {
			final String maybe = operationUser().getPreferredGroup().getName();
			final String expression = performer.getValue();

			final TemplateResolver templateResolver = activityPerformerTemplateResolverFactory().create();
			final String resolvedExpression = templateResolver.resolve(expression);

			final ActivityPerformerExpressionEvaluator evaluator = new BshActivityPerformerExpressionEvaluator(
					resolvedExpression);
			final Set<String> names = evaluator.getNames();
			performerName = names.contains(maybe) ? maybe : StringUtils.EMPTY;
			break;
		}
		default: {
			performerName = StringUtils.EMPTY;
			break;
		}
		}
		return performerName;
	}

	@JSONExported
	public JsonResponse getActivityInstance( //
			@Parameter(CLASS_ID) final Long processClassId, //
			@Parameter(CARD_ID) final Long processInstanceId, //
			@Parameter(ACTIVITY_INSTANCE_ID) final String activityInstanceId //
	) throws CMWorkflowException {
		final UserActivityInstance activityInstance = workflowLogic().getActivityInstance( //
				processClassId, processInstanceId, activityInstanceId);

		return JsonResponse.success(new JsonActivityInstance(activityInstance));
	}

	@JSONExported
	@SuppressWarnings("serial")
	public JsonResponse isProcessUpdated( //
			@Parameter(CLASS_NAME) final String processClassName, //
			@Parameter(PROCESS_INSTANCE_ID) final Long processInstanceId, //
			@Parameter(BEGIN_DATE) final long beginDateAsLong) {

		final DateTime givenBeginDate = new DateTime(beginDateAsLong);
		final WorkflowLogic logic = workflowLogic();
		final boolean isUpdated = logic.isProcessUpdated(processClassName, processInstanceId, givenBeginDate);

		if (!isUpdated) {
			throw ConsistencyExceptionType.OUT_OF_DATE_PROCESS.createException();
		}

		return JsonResponse.success(new HashMap<String, Object>() {
			{
				put("updated", isUpdated);
			}
		});
	}

	@JSONExported
	@SuppressWarnings("serial")
	public JsonResponse saveActivity( //
			@Parameter(CLASS_ID) final Long processClassId, //
			// processCardId even with Long, it won't be null
			@Parameter(value = CARD_ID, required = false) final long processCardId, //
			@Parameter(value = ACTIVITY_INSTANCE_ID, required = false) final String activityInstanceId, //
			@Parameter(ATTRIBUTES) final String jsonVars, //
			@Parameter(ADVANCE) final boolean advance, //
			@Parameter(WW) final String jsonWidgetSubmission //
	) throws CMWorkflowException, Exception {
		final WorkflowLogic logic = workflowLogic();
		final CMProcessInstance processInstance;
		@SuppressWarnings("unchecked")
		final Map<String, Object> vars = new ObjectMapper().readValue(jsonVars, Map.class);
		@SuppressWarnings("unchecked")
		final Map<String, Object> widgetSubmission = new ObjectMapper().readValue(jsonWidgetSubmission, Map.class);

		if (processCardId > 0) { // should check for null
			processInstance = logic.updateProcess(processClassId, processCardId, activityInstanceId, vars,
					widgetSubmission, advance);
		} else {
			processInstance = logic.startProcess(processClassId, vars, widgetSubmission, advance);
		}

		final DateTimeFormatter formatter = DateTimeFormat.forPattern(Constants.DATETIME_FOUR_DIGIT_YEAR_FORMAT);
		final DateTime beginDate = processInstance.getBeginDate();
		return JsonResponse.success(new HashMap<String, Object>() {
			{
				put("Id", processInstance.getCardId());
				put("IdClass", processInstance.getType().getId());
				put("ProcessInstanceId", processInstance.getProcessInstanceId());
				put("beginDate", formatter.print(beginDate));
				put("beginDateAsLong", processInstance.getBeginDate().getMillis());
				put("flowStatus", processInstance.getState().name());
			}
		});
	}

	@JSONExported
	public JsonResponse abortprocess( //
			@Parameter(CLASS_ID) final Long processClassId, //
			@Parameter(CARD_ID) final long processCardId //
	) throws CMWorkflowException {
		workflowLogic().abortProcess(processClassId, processCardId);

		return JsonResponse.success(null);
	}

	@Admin
	@JSONExported
	public DataHandler downloadXpdlTemplate( //
			@Parameter(ID_CLASS) final Long processClassId //
	) throws CMWorkflowException {
		final DataSource ds = workflowLogic().getProcessDefinitionTemplate(processClassId);

		return new DataHandler(ds);
	}

	@Admin
	@JSONExported
	public JsonResponse xpdlVersions( //
			@Parameter(value = ID_CLASS, required = true) final Long processClassId //
	) throws CMWorkflowException {
		final String[] versions = workflowLogic().getProcessDefinitionVersions(processClassId);

		return JsonResponse.success(versions);
	}

	@Admin
	@JSONExported
	public DataHandler downloadXpdl( //
			@Parameter(ID_CLASS) final Long processClassId, //
			@Parameter(VERSION) final String version //
	) throws CMWorkflowException {
		final DataSource ds = workflowLogic().getProcessDefinition(processClassId, version);

		return new DataHandler(ds);
	}

	@Admin
	@JSONExported
	public JsonResponse uploadXpdl( //
			@Parameter(ID_CLASS) final Long processClassId, //
			@Parameter(value = XPDL, required = false) final FileItem xpdlFile //
	) throws CMWorkflowException {
		final List<String> messages = Lists.newArrayList();
		final WorkflowLogic logic = workflowLogic();
		if (xpdlFile.getSize() != 0) {
			logic.updateProcessDefinition(processClassId, wrapAsDataSource(xpdlFile));
			messages.add("saved_xpdl");
		}

		return JsonResponse.success(messages);
	}

	private DataSource wrapAsDataSource(final FileItem xpdlFile) {
		return new DataSource() {
			@Override
			public String getContentType() {
				return xpdlFile.getContentType();
			}

			@Override
			public InputStream getInputStream() throws IOException {
				return xpdlFile.getInputStream();
			}

			@Override
			public String getName() {
				return xpdlFile.getName();
			}

			@Override
			public OutputStream getOutputStream() throws IOException {
				return xpdlFile.getOutputStream();
			}
		};
	}

	@Admin
	@JSONExported
	public void sync() throws CMWorkflowException {
		workflowLogic().sync();
	}

}
