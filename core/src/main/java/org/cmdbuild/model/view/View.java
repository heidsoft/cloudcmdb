package org.cmdbuild.model.view;

import org.cmdbuild.auth.acl.SerializablePrivilege;
import org.cmdbuild.services.localization.LocalizableStorable;

public interface View extends LocalizableStorable, SerializablePrivilege {

	public enum ViewType {
		SQL, FILTER
	}

	@Override
	Long getId();

	@Override
	String getName();

	@Override
	String getDescription();

	String getSourceClassName();

	String getSourceFunction();

	String getFilter();

	ViewType getType();

	@Override
	String getIdentifier();

	@Override
	String getPrivilegeId();

}