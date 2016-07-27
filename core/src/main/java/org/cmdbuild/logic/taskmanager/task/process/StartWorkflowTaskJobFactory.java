package org.cmdbuild.logic.taskmanager.task.process;

import static org.cmdbuild.common.utils.BuilderUtils.a;

import org.cmdbuild.logic.taskmanager.commons.SchedulerCommandWrapper;
import org.cmdbuild.logic.taskmanager.scheduler.AbstractJobFactory;
import org.cmdbuild.logic.workflow.StartProcess;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.scheduler.command.Command;

public class StartWorkflowTaskJobFactory extends AbstractJobFactory<StartWorkflowTask> {

	private final WorkflowLogic workflowLogic;

	public StartWorkflowTaskJobFactory(final WorkflowLogic workflowLogic) {
		this.workflowLogic = workflowLogic;
	}

	@Override
	protected Class<StartWorkflowTask> getType() {
		return StartWorkflowTask.class;
	}

	@Override
	protected Command command(final StartWorkflowTask task) {
		final StartProcess startProcess = a(StartProcess.newInstance() //
				.withWorkflowLogic(workflowLogic) //
				.withClassName(task.getProcessClass()) //
				.withAttributes(task.getAttributes()));
		return SchedulerCommandWrapper.of(startProcess);
	}

}
