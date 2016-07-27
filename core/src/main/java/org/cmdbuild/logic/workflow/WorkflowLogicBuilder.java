package org.cmdbuild.logic.workflow;

import org.apache.commons.lang3.builder.Builder;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.config.WorkflowConfiguration;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.LockLogic;
import org.cmdbuild.services.FilesStore;
import org.cmdbuild.workflow.DefaultWorkflowEngine;
import org.cmdbuild.workflow.QueryableUserWorkflowEngine;

public abstract class WorkflowLogicBuilder implements Builder<WorkflowLogic> {

	private final PrivilegeContext privilegeContext;
	private final QueryableUserWorkflowEngine workflowEngine;
	private final CMDataView dataView;
	private final WorkflowConfiguration configuration;
	private final FilesStore filesStore;
	private final LockLogic lockLogic;

	protected WorkflowLogicBuilder( //
			final PrivilegeContext privilegeContext, //
			final Builder<DefaultWorkflowEngine> workflowEngineBuilder, //
			final CMDataView dataView, //
			final WorkflowConfiguration configuration, //
			final FilesStore filesStore, //
			final LockLogic lockLogic //
	) {
		this.privilegeContext = privilegeContext;
		this.workflowEngine = workflowEngineBuilder.build();
		this.dataView = dataView;
		this.configuration = configuration;
		this.filesStore = filesStore;
		this.lockLogic = lockLogic;
	}

	@Override
	public WorkflowLogic build() {
		return new DefaultWorkflowLogic( //
				privilegeContext, //
				workflowEngine, //
				dataView, //
				configuration, //
				filesStore, //
				lockLogic);
	}

}
