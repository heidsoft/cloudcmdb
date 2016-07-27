package org.cmdbuild.workflow.api;

import org.cmdbuild.api.fluent.ExistingProcessInstance;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.api.fluent.ForwardingFluentApiExecutor;

import com.google.common.base.Optional;

public class SharkFluentApiExecutor extends ForwardingFluentApiExecutor {

	private final FluentApiExecutor delegate;
	private final Optional<Long> processId;
	private final SelfSuspensionRequestHolder selfSuspensionRequestHolder;

	public SharkFluentApiExecutor(final FluentApiExecutor delegate, final Optional<Long> processId,
			final SelfSuspensionRequestHolder selfSuspensionRequestHolder) {
		this.delegate = delegate;
		this.processId = processId;
		this.selfSuspensionRequestHolder = selfSuspensionRequestHolder;
	}

	@Override
	protected FluentApiExecutor delegate() {
		return delegate;
	}

	@Override
	public void suspendProcessInstance(final ExistingProcessInstance processCard) {
		final boolean hold;
		if (processId.isPresent()) {
			hold = processId.get().equals(processCard.getId().longValue());
		} else {
			hold = false;
		}

		if (hold) {
			selfSuspensionRequestHolder.add(processId.get());
		} else {
			super.suspendProcessInstance(processCard);
		}
	}

}
