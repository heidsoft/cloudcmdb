package org.cmdbuild.workflow;

public interface Constants {

	String PROCESS_CARD_ID_VARIABLE = "ProcessId";
	String PROCESS_CLASSNAME_VARIABLE = "ProcessClass";
	String PROCESS_INSTANCE_ID_VARIABLE = "ProcessCode";

	String CURRENT_USER_USERNAME_VARIABLE = "_CurrentUserUsername";
	String CURRENT_GROUP_NAME_VARIABLE = "_CurrentGroupName";
	String CURRENT_USER_VARIABLE = "_CurrentUser";
	/**
	 * variable name is misleading but we cannot change it
	 */
	String CURRENT_PERFORMER_VARIABLE = "_CurrentGroup";
	String API_VARIABLE = "cmdb";

	String XPDL_REFERENCE_DECLARED_TYPE = "Reference";
	String XPDL_LOOKUP_DECLARED_TYPE = "Lookup";
	String XPDL_ARRAY_DECLARED_TYPE_SUFFIX = "s";
	String XPDL_REFERENCE_ARRAY_DECLARED_TYPE = XPDL_REFERENCE_DECLARED_TYPE + XPDL_ARRAY_DECLARED_TYPE_SUFFIX;
	String XPDL_LOOKUP_ARRAY_DECLARED_TYPE = XPDL_LOOKUP_DECLARED_TYPE + XPDL_ARRAY_DECLARED_TYPE_SUFFIX;

}
