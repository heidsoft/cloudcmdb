package org.cmdbuild.api.fluent;

import org.cmdbuild.api.fluent.FluentApiExecutor.AdvanceProcess;

public class ExistingProcessInstance extends ActiveCard {

	ExistingProcessInstance(final FluentApiExecutor executor, final String className, final Integer processId) {
		super(executor, className, processId);
	}

	public ExistingProcessInstance withProcessInstanceId(final String value) {
		super.set("ProcessCode", value);
		return this;
	}

	public ExistingProcessInstance withDescription(final String value) {
		super.setDescription(value);
		return this;
	}

	public ExistingProcessInstance with(final String name, final Object value) {
		return withAttribute(name, value);
	}

	public ExistingProcessInstance withAttribute(final String name, final Object value) {
		super.set(name, value);
		return this;
	}

	public void update() {
		executor().updateProcessInstance(this, AdvanceProcess.NO);
	}

	public void advance() {
		executor().updateProcessInstance(this, AdvanceProcess.YES);
	}

	public void suspend() {
		executor().suspendProcessInstance(this);
	}

	public void resume() {
		executor().resumeProcessInstance(this);
	}

	public Attachments attachments() {
		return new AttachmentsImpl(executor(), this);
	}

	public void abort() {
		executor().abortProcessInstance(this);
	}

}
