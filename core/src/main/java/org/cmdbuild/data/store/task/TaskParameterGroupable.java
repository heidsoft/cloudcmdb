package org.cmdbuild.data.store.task;

import static org.cmdbuild.data.store.Groupables.nameAndValue;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.data.store.ForwardingGroupable;
import org.cmdbuild.data.store.Groupable;

public class TaskParameterGroupable extends ForwardingGroupable {

	public static TaskParameterGroupable groupedBy(final TaskDefinition owner) {
		Validate.notNull(owner, "owner cannot be null");
		return new TaskParameterGroupable(nameAndValue(TaskParameterConverter.OWNER, owner.getId()));
	}

	private final Groupable delegate;

	private TaskParameterGroupable(final Groupable delegate) {
		this.delegate = delegate;
	}

	@Override
	protected Groupable delegate() {
		return delegate;
	}

}