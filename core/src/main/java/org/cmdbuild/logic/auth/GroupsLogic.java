package org.cmdbuild.logic.auth;

import org.cmdbuild.auth.acl.CMGroup;

public interface GroupsLogic {

	CMGroup createGroup(GroupDTO groupDTO);

	CMGroup updateGroup(GroupDTO groupDTO);

	CMGroup setGroupActive(Long groupId, boolean active);

	void addUserToGroup(Long userId, Long groupId);

	void removeUserFromGroup(Long userId, Long groupId);

}
