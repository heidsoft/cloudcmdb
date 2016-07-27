package org.cmdbuild.workflow.api;

public interface Impersonate {

	Impersonate username(String username);

	Impersonate group(String group);

	WorkflowApi impersonate();

}
