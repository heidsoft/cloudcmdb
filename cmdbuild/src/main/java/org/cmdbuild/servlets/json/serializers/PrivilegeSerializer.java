package org.cmdbuild.servlets.json.serializers;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.logic.privileges.PrivilegeInfo.EMPTY_ATTRIBUTES_PRIVILEGES;
import static org.cmdbuild.servlets.json.CommunicationConstants.ATTRIBUTES_PRIVILEGES;
import static org.cmdbuild.servlets.json.CommunicationConstants.GROUP_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.PRIVILEGES;
import static org.cmdbuild.servlets.json.CommunicationConstants.PRIVILEGE_FILTER;
import static org.cmdbuild.servlets.json.CommunicationConstants.PRIVILEGE_NONE;
import static org.cmdbuild.servlets.json.CommunicationConstants.PRIVILEGE_OBJ_DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.PRIVILEGE_OBJ_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.PRIVILEGE_OBJ_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.PRIVILEGE_READ;
import static org.cmdbuild.servlets.json.CommunicationConstants.PRIVILEGE_WRITE;

import java.util.List;

import org.cmdbuild.auth.privileges.constants.PrivilegeMode;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.privileges.PrivilegeInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PrivilegeSerializer {

	public static JSONObject serializePrivilege(final PrivilegeInfo privilege) throws JSONException {
		final JSONObject jsonPrivilege = new JSONObject();
		jsonPrivilege.put(GROUP_ID, privilege.getGroupId());
		jsonPrivilege.put(PRIVILEGE_WRITE, privilege.getMode().getValue().equals(PrivilegeMode.WRITE.getValue()));
		jsonPrivilege.put(PRIVILEGE_READ, privilege.getMode().getValue().equals(PrivilegeMode.READ.getValue()));
		jsonPrivilege.put(PRIVILEGE_NONE, privilege.getMode().getValue().equals(PrivilegeMode.NONE.getValue()));
		jsonPrivilege.put(PRIVILEGE_OBJ_ID, privilege.getPrivilegedObjectId());
		jsonPrivilege.put(PRIVILEGE_OBJ_NAME, privilege.getPrivilegedObjectName());
		jsonPrivilege.put(PRIVILEGE_OBJ_DESCRIPTION, privilege.getPrivilegedObjectDescription());
		jsonPrivilege.put(PRIVILEGE_FILTER, privilege.getPrivilegeFilter());
		jsonPrivilege.put(ATTRIBUTES_PRIVILEGES,
				defaultIfNull(privilege.getAttributesPrivileges(), EMPTY_ATTRIBUTES_PRIVILEGES));
		// TODO serialize cardEditMode too
		return jsonPrivilege;
	}

	public static JSONObject serializePrivilegeList(final List<PrivilegeInfo> privileges) throws JSONException {
		final JSONArray privilegeList = new JSONArray();
		final JSONObject out = new JSONObject();
		for (final PrivilegeInfo privilege : privileges) {
			try {
				privilegeList.put(serializePrivilege(privilege));
			} catch (final NotFoundException e) {
				Log.PERSISTENCE.warn( //
						"Class OID not found (" + //
								privilege.getPrivilegedObjectId() + //
								") while searching for grant for group " + //
								privilege.getGroupId() //
				);
			}
		}

		out.put(PRIVILEGES, privilegeList);
		return out;
	}

}
