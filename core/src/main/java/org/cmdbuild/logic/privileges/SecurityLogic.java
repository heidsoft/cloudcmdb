package org.cmdbuild.logic.privileges;

import java.util.List;

import org.cmdbuild.model.profile.UIConfiguration;

public interface SecurityLogic {

	String GROUP_ATTRIBUTE_DISABLEDMODULES = "DisabledModules";
	String GROUP_ATTRIBUTE_DISABLEDCARDTABS = "DisabledCardTabs";
	String GROUP_ATTRIBUTE_DISABLEDPROCESSTABS = "DisabledProcessTabs";
	String GROUP_ATTRIBUTE_HIDESIDEPANEL = "HideSidePanel";
	String GROUP_ATTRIBUTE_FULLSCREEN = "FullScreenMode";
	String GROUP_ATTRIBUTE_SIMPLE_HISTORY_CARD = "SimpleHistoryModeForCard";
	String GROUP_ATTRIBUTE_SIMPLE_HISTORY_PROCESS = "SimpleHistoryModeForProcess";
	String GROUP_ATTRIBUTE_PROCESS_WIDGET_ALWAYS_ENABLED = "ProcessWidgetAlwaysEnabled";
	String GROUP_ATTRIBUTE_CLOUD_ADMIN = "CloudAdmin";

	List<PrivilegeInfo> fetchClassPrivilegesForGroup(Long groupId);

	List<PrivilegeInfo> fetchProcessPrivilegesForGroup(Long groupId);

	CardEditMode fetchCardEditModeForGroupAndClass(Long groupId, Long classId);

	List<PrivilegeInfo> fetchViewPrivilegesForGroup(Long groupId);

	List<PrivilegeInfo> fetchFilterPrivilegesForGroup(Long groupId);

	List<PrivilegeInfo> fetchCustomViewPrivilegesForGroup(Long groupId);

	/*
	 * FIXME
	 * 
	 * this methods is called for two different purposes
	 * 
	 * 1) change class-mode
	 * 
	 * 2) change row and column privileges configuration
	 * 
	 * Remove the mode only flag and implement two different methods or uniform
	 * the values set in the privilegeInfo object in order to have all the
	 * attributes and update them all
	 */
	void saveClassPrivilege(PrivilegeInfo privilegeInfo, boolean modeOnly);

	void saveProcessPrivilege(PrivilegeInfo privilegeInfo, boolean modeOnly);

	void saveViewPrivilege(PrivilegeInfo privilegeInfo);

	void saveFilterPrivilege(PrivilegeInfo privilegeInfo);

	void saveCustomPagePrivilege(PrivilegeInfo privilegeInfo);

	UIConfiguration fetchGroupUIConfiguration(Long groupId);

	void saveGroupUIConfiguration(Long groupId, UIConfiguration configuration);

	void saveCardEditMode(PrivilegeInfo privilegeInfoToSave);

}