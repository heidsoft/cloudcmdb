package org.cmdbuild.workflow.api;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.synchronizedSet;

import java.util.Collection;
import java.util.HashSet;

public class MonostateSelfSuspensionRequestHolder implements SelfSuspensionRequestHolder {

	private static Collection<Long> processInstancesToSuspend;

	static {
		final HashSet<Long> set = newHashSet();
		processInstancesToSuspend = synchronizedSet(set);
	}

	@Override
	public void add(final Long processInstanceId) {
		processInstancesToSuspend.add(processInstanceId);
	}

	@Override
	public boolean remove(final Long processInstanceId) {
		return processInstancesToSuspend.remove(processInstanceId);
	}

}
