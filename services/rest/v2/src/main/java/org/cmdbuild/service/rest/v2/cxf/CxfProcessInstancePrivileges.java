package org.cmdbuild.service.rest.v2.cxf;

import static org.cmdbuild.service.rest.v2.model.Models.newProcessInstancePrivileges;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;

import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.v2.ProcessInstancePrivileges;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.cmdbuild.workflow.user.UserProcessClass;

public class CxfProcessInstancePrivileges implements ProcessInstancePrivileges {

	private final ErrorHandler errorHandler;
	private final WorkflowLogic workflowLogic;

	public CxfProcessInstancePrivileges(final ErrorHandler errorHandler, final WorkflowLogic workflowLogic) {
		this.errorHandler = errorHandler;
		this.workflowLogic = workflowLogic;
	}

	@Override
	public ResponseSingle<org.cmdbuild.service.rest.v2.model.ProcessInstancePrivileges> read(final String processId,
			final Long processInstanceId) {
		final UserProcessClass found = workflowLogic.findProcessClass(processId);
		if (found == null) {
			errorHandler.processNotFound(processId);
		}
		return newResponseSingle(org.cmdbuild.service.rest.v2.model.ProcessInstancePrivileges.class) //
				.withElement(newProcessInstancePrivileges() //
						.stoppable(found.isStoppable()) //
						.build()) //
				.build();
	}

}
