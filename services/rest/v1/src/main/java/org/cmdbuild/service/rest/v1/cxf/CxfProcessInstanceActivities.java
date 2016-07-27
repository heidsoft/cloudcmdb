package org.cmdbuild.service.rest.v1.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Iterables.size;
import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.v1.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v1.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v1.model.Models.newResponseSingle;

import java.util.List;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.mapping.json.FilterElementGetters;
import org.cmdbuild.logic.mapping.json.JsonFilterHelper;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.v1.ProcessInstanceActivities;
import org.cmdbuild.service.rest.v1.cxf.serialization.ToProcessActivityDefinition;
import org.cmdbuild.service.rest.v1.cxf.serialization.ToProcessActivityWithBasicDetailsFromUserActivityInstance;
import org.cmdbuild.service.rest.v1.model.ProcessActivityWithBasicDetails;
import org.cmdbuild.service.rest.v1.model.ProcessActivityWithFullDetails;
import org.cmdbuild.service.rest.v1.model.ResponseMultiple;
import org.cmdbuild.service.rest.v1.model.ResponseSingle;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMActivityWidget;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.ForwardingActivity;
import org.cmdbuild.workflow.user.UserActivityInstance;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Predicates;

public class CxfProcessInstanceActivities implements ProcessInstanceActivities {

	private static final ToProcessActivityWithBasicDetailsFromUserActivityInstance TO_OUTPUT = ToProcessActivityWithBasicDetailsFromUserActivityInstance
			.newInstance() //
			.build();

	private final ErrorHandler errorHandler;
	private final WorkflowLogic workflowLogic;

	public CxfProcessInstanceActivities(final ErrorHandler errorHandler, final WorkflowLogic workflowLogic) {
		this.errorHandler = errorHandler;
		this.workflowLogic = workflowLogic;
	}

	@Override
	public ResponseMultiple<ProcessActivityWithBasicDetails> read(final String processId, final Long processInstanceId) {
		final UserProcessClass found = workflowLogic.findProcessClass(processId);
		if (found == null) {
			errorHandler.processNotFound(processId);
		}
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.limit(1) //
				.offset(0) //
				.filter(filterFor(processInstanceId)) //
				.build();
		final PagedElements<UserProcessInstance> elements = workflowLogic.query(found, queryOptions);
		if (elements.totalSize() == 0) {
			errorHandler.processInstanceNotFound(processInstanceId);
		}
		final Iterable<UserActivityInstance> activities = getOnlyElement(elements).getActivities();
		return newResponseMultiple(ProcessActivityWithBasicDetails.class) //
				.withElements(from(activities) //
						.transform(TO_OUTPUT) //
				) //
				.withMetadata(newMetadata() //
						.withTotal(Long.valueOf(size(activities))) //
						.build() //
				).build();
	}

	private JSONObject filterFor(final Long id) {
		try {
			final JSONObject emptyFilter = new JSONObject();
			return new JsonFilterHelper(emptyFilter) //
					.merge(FilterElementGetters.id(id));
		} catch (final JSONException e) {
			errorHandler.propagate(e);
			return new JSONObject();
		}
	}

	@Override
	public ResponseSingle<ProcessActivityWithFullDetails> read(final String processId, final Long processInstanceId,
			final String processActivityId) {
		final UserProcessClass foundType = workflowLogic.findProcessClass(processId);
		if (foundType == null) {
			errorHandler.processNotFound(processId);
		}
		final UserProcessInstance foundInstance = workflowLogic.getProcessInstance(processId, processInstanceId);
		if (foundInstance == null) {
			errorHandler.processInstanceNotFound(processInstanceId);
		}
		final UserActivityInstance activityInstance = workflowLogic.getActivityInstance(processId, processInstanceId,
				processActivityId);
		if (activityInstance == null) {
			errorHandler.processActivityNotFound(processActivityId);
		}
		try {
			final CMActivity delegate = activityInstance.getDefinition();
			final CMActivity foundActivity = new ForwardingActivity() {

				@Override
				protected CMActivity delegate() {
					return delegate;
				}

				@Override
				public List<CMActivityWidget> getWidgets() throws CMWorkflowException {
					return activityInstance.getWidgets();
				}

			};
			final ToProcessActivityDefinition TO_PROCESS_ACTIVITY = ToProcessActivityDefinition.newInstance() //
					.withWritableStatus(activityInstance.isWritable()) //
					.build();
			return newResponseSingle(ProcessActivityWithFullDetails.class) //
					.withElement(from(asList(foundActivity)) //
							.filter(Predicates.notNull()) //
							.transform(TO_PROCESS_ACTIVITY) //
							.first() //
							.get()) //
					.build();
		} catch (final CMWorkflowException e) {
			errorHandler.propagate(e);
			return null;
		}
	}

}
