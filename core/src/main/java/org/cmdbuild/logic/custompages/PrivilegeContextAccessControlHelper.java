package org.cmdbuild.logic.custompages;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.logic.custompages.DefaultCustomPagesLogic.AccessControlHelper;
import org.cmdbuild.privileges.CustomPageAdapter;

public class PrivilegeContextAccessControlHelper implements AccessControlHelper {

	private final UserStore userStore;

	public PrivilegeContextAccessControlHelper(final UserStore userStore) {
		this.userStore = userStore;
	}

	@Override
	public boolean isAccessible(CustomPage value) {
		return userStore.getUser().getPrivilegeContext().hasReadAccess(new CustomPageAdapter(value));
	}

}
