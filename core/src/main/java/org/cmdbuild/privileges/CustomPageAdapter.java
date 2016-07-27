package org.cmdbuild.privileges;

import static java.lang.String.format;

import org.cmdbuild.auth.acl.SerializablePrivilege;
import org.cmdbuild.logic.custompages.CustomPage;

public class CustomPageAdapter implements SerializablePrivilege {

	private final CustomPage delegate;

	public CustomPageAdapter(final CustomPage delegate) {
		this.delegate = delegate;
	}

	@Override
	public String getPrivilegeId() {
		return format("CustomPage:%d", getId());
	}

	@Override
	public Long getId() {
		return delegate.getId();
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public String getDescription() {
		return delegate.getDescription();
	}

}