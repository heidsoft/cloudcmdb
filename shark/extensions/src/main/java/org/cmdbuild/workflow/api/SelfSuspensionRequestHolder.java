package org.cmdbuild.workflow.api;

public interface SelfSuspensionRequestHolder {

	void add(Long processInstanceId);

	boolean remove(Long processInstanceId);

}