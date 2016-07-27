package org.cmdbuild.service.rest.v2.cxf;

import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;

import org.cmdbuild.common.utils.UnsupportedProxyFactory;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.v2.ProcessStartActivities;
import org.cmdbuild.service.rest.v2.cxf.serialization.ToProcessActivityDefinition;
import org.cmdbuild.service.rest.v2.cxf.serialization.ToProcessActivityWithBasicDetailsFromCMActivity;
import org.cmdbuild.service.rest.v2.model.ProcessActivityWithBasicDetails;
import org.cmdbuild.service.rest.v2.model.ProcessActivityWithFullDetails;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.user.UserProcessClass;

public class CxfProcessStartActivities implements ProcessStartActivities {

	private static final CMActivity UNSUPPORTED_ACTIVITY = UnsupportedProxyFactory.of(CMActivity.class).create();

	private static final ToProcessActivityWithBasicDetailsFromCMActivity TO_ACTIVITY_BASIC = ToProcessActivityWithBasicDetailsFromCMActivity
			.newInstance().build();
	private static final ToProcessActivityDefinition TO_ACTIVITY_FULL = ToProcessActivityDefinition.newInstance()
			.withWritableStatus(true) //
			.build();

	private final ErrorHandler errorHandler;
	private final WorkflowLogic workflowLogic;

	public CxfProcessStartActivities(final ErrorHandler errorHandler, final WorkflowLogic workflowLogic) {
		this.errorHandler = errorHandler;
		this.workflowLogic = workflowLogic;
	}

	@Override
	public ResponseMultiple<ProcessActivityWithBasicDetails> read(final String processId) {
		final UserProcessClass found = workflowLogic.findProcessClass(processId);
		if (found == null) {
			errorHandler.processNotFound(processId);
		}
		final CMActivity activity = startActivityFor(processId);
		final ProcessActivityWithBasicDetails element = TO_ACTIVITY_BASIC.apply(activity);
		return newResponseMultiple(ProcessActivityWithBasicDetails.class) //
				.withElement(element) //
				.withMetadata(newMetadata() //
						.withTotal(1L) //
						.build()) //
				.build();
	}

	@Override
	public ResponseSingle<ProcessActivityWithFullDetails> read(final String processId, final String activityId) {
		final UserProcessClass found = workflowLogic.findProcessClass(processId);
		if (found == null) {
			errorHandler.processNotFound(processId);
		}
		final CMActivity activity = startActivityFor(processId);
		if (!activity.getId().equals(activityId)) {
			errorHandler.processActivityNotFound(activityId);
		}
		final ProcessActivityWithFullDetails element = TO_ACTIVITY_FULL.apply(activity);
		return newResponseSingle(ProcessActivityWithFullDetails.class) //
				.withElement(element) //
				.build();
	}

	private CMActivity startActivityFor(final String processId) {
		try {
			return workflowLogic.getStartActivity(processId);
		} catch (final CMWorkflowException e) {
			errorHandler.propagate(e);
			return UNSUPPORTED_ACTIVITY;
		}
	}

}
