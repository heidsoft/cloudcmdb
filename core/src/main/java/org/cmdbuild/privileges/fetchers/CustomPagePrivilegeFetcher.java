package org.cmdbuild.privileges.fetchers;

import static org.cmdbuild.auth.privileges.constants.GrantConstants.MODE_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.PRIVILEGED_OBJECT_ID_ATTRIBUTE;

import java.util.NoSuchElementException;

import org.cmdbuild.auth.acl.CMPrivilege;
import org.cmdbuild.auth.acl.DefaultPrivileges;
import org.cmdbuild.auth.acl.SerializablePrivilege;
import org.cmdbuild.auth.privileges.constants.PrivilegeMode;
import org.cmdbuild.auth.privileges.constants.PrivilegedObjectType;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.custompages.CustomPagesLogic;
import org.cmdbuild.privileges.CustomPageAdapter;

public class CustomPagePrivilegeFetcher extends AbstractPrivilegeFetcher {

	private final CustomPagesLogic customPagesLogic;

	public CustomPagePrivilegeFetcher(final CMDataView view, final Long groupId, final CustomPagesLogic customPagesLogic) {
		super(view, groupId);
		this.customPagesLogic = customPagesLogic;
	}

	@Override
	protected PrivilegedObjectType getPrivilegedObjectType() {
		return PrivilegedObjectType.CUSTOMPAGE;
	}

	@Override
	protected SerializablePrivilege extractPrivilegedObject(final CMCard privilegeCard) {
		final Integer filterId = privilegeCard.get(PRIVILEGED_OBJECT_ID_ATTRIBUTE, Integer.class);
		try {
			return new CustomPageAdapter(customPagesLogic.read(filterId.longValue()));
		} catch (final NoSuchElementException ex) {
			Log.CMDBUILD.warn("Cannot fetch filter with id {}. Check all references to that filter in Grant table",
					filterId);
		}
		return null;
	}

	@Override
	protected CMPrivilege extractPrivilegeMode(final CMCard privilegeCard) {
		final Object type = privilegeCard.get(MODE_ATTRIBUTE);
		if (PrivilegeMode.READ.getValue().equals(type)) {
			return DefaultPrivileges.READ;
		} else if (PrivilegeMode.WRITE.getValue().equals(type)) {
			return DefaultPrivileges.WRITE;
		}
		return DefaultPrivileges.NONE;
	}
}
