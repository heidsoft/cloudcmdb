package org.cmdbuild.servlets.json.serializers;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.EMAIL;
import static org.cmdbuild.servlets.json.CommunicationConstants.IS_ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.META;
import static org.cmdbuild.servlets.json.CommunicationConstants.PRIVILEGED;
import static org.cmdbuild.servlets.json.CommunicationConstants.SERVICE;
import static org.cmdbuild.servlets.json.CommunicationConstants.USER_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.USER_NAME;
import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.Metadata;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.exception.DmsException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.auth.AuthenticationLogic.GroupInfo;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.store.report.Report;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Serializer {

	// TODO use constants
	private static final SimpleDateFormat ATTACHMENT_DATE_FOMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	public static final String AVAILABLE_CLASS = "availableclass";
	public static final String AVAILABLE_PROCESS_CLASS = "availableprocessclass";
	public static final String AVAILABLE_REPORT = "availablereport";
	public static final String AVAILABLE_DASHBOARDS = "availabledashboards";

	private static final Iterable<MetadataGroup> NO_METADATA_GROUPS = emptyList();

	public static JSONObject serializeAttachment(final StoredDocument attachment) {
		final JSONObject serializer = new JSONObject();
		try {
			serializer.put("Category", attachment.getCategory());
			serializer.put("CreationDate", ATTACHMENT_DATE_FOMAT.format(attachment.getCreated()));
			serializer.put("ModificationDate", ATTACHMENT_DATE_FOMAT.format(attachment.getModified()));
			serializer.put("Author", attachment.getAuthor());
			serializer.put("Version", attachment.getVersion());
			serializer.put("Filename", attachment.getName());
			serializer.put("Description", attachment.getDescription());
			serializer.put("Metadata", serialize(attachment.getMetadataGroups()));
		} catch (final JSONException e) {
			Log.JSONRPC.error("Error serializing attachment", e);
		}
		return serializer;
	}

	private static JSONObject serialize(final Iterable<MetadataGroup> metadataGroups) throws JSONException {
		final JSONObject jsonMetadata = new JSONObject();
		for (final MetadataGroup metadataGroup : defaultIfNull(metadataGroups, NO_METADATA_GROUPS)) {
			final JSONObject jsonAllMetadata = new JSONObject();
			for (final Metadata metadata : metadataGroup.getMetadata()) {
				jsonAllMetadata.put(metadata.getName(), metadata.getValue());
			}
			jsonMetadata.put(metadataGroup.getName(), jsonAllMetadata);
		}
		return jsonMetadata;
	}

	// FIXME: implement it reading the metadata for the class
	protected static void addMetadata(final JSONObject serializer) throws JSONException {
		final JSONObject jsonMetadata = new JSONObject();
		serializer.put(META, jsonMetadata);
	}

	public static JSONArray buildJsonAvaiableMenuItems() throws JSONException {
		final JSONArray jsonAvaiableItems = new JSONArray();

		final JSONObject jsonClassesFolder = new JSONObject();
		final JSONObject jsonReportsFolder = new JSONObject();
		final JSONObject jsonProcessFolder = new JSONObject();
		final JSONObject jsonDashboardsFolder = new JSONObject();

		jsonClassesFolder.put("text", "class");
		jsonClassesFolder.put("id", AVAILABLE_CLASS);
		jsonClassesFolder.put("iconCls", "cmdbuild-tree-folder-icon");
		jsonClassesFolder.put("cmIndex", 1);

		jsonProcessFolder.put("text", "processclass");
		jsonProcessFolder.put("id", AVAILABLE_PROCESS_CLASS);
		jsonProcessFolder.put("iconCls", "cmdbuild-tree-folder-icon");
		jsonProcessFolder.put("cmIndex", 2);

		jsonReportsFolder.put("text", "report");
		jsonReportsFolder.put("id", AVAILABLE_REPORT);
		jsonReportsFolder.put("iconCls", "cmdbuild-tree-folder-icon");
		jsonReportsFolder.put("cmIndex", 3);

		jsonDashboardsFolder.put("text", "dashboard");
		jsonDashboardsFolder.put("id", AVAILABLE_DASHBOARDS);
		jsonDashboardsFolder.put("iconCls", "cmdbuild-tree-folder-icon");
		jsonDashboardsFolder.put("cmIndex", 4);

		jsonAvaiableItems.put(jsonReportsFolder);
		jsonAvaiableItems.put(jsonClassesFolder);
		jsonAvaiableItems.put(jsonProcessFolder);
		jsonAvaiableItems.put(jsonDashboardsFolder);

		return jsonAvaiableItems;
	}

	public static JSONObject serializeReportForMenu(final Report report, final String type) throws JSONException {
		final JSONObject jsonReport = new JSONObject();
		jsonReport.put("text", report.getDescription());
		jsonReport.put("parent", AVAILABLE_REPORT);
		jsonReport.put("selectable", true);
		jsonReport.put("type", type);
		jsonReport.put("subtype", report.getType().toString().toLowerCase());
		jsonReport.put("objid", report.getId());
		jsonReport.put("id", report.getId() + type);
		jsonReport.put("leaf", true);
		return jsonReport;
	}

	public static JSONObject serialize(final CMGroup group) throws JSONException {
		final JSONObject jsonGroup = new JSONObject();
		jsonGroup.put("id", group.getId());
		jsonGroup.put("name", group.getName());
		jsonGroup.put("description", group.getDescription());
		jsonGroup.put("email", group.getEmail());
		jsonGroup.put("isAdministrator", group.isAdmin());
		jsonGroup.put("isCloudAdministrator", group.isRestrictedAdmin());
		// TODO check if missing
		jsonGroup.put("startingClass", group.getStartingClassId());
		jsonGroup.put("isActive", group.isActive());
		jsonGroup.put("text", group.getDescription());
		jsonGroup.put("selectable", true);
		jsonGroup.put("type", "group");
		return jsonGroup;
	}

	public static JSONArray serializeGroupsForUser(final CMUser user, final List<GroupInfo> groups)
			throws JSONException {
		final JSONArray jsonGroupList = new JSONArray();
		for (final GroupInfo group : groups) {
			final JSONObject row = new JSONObject();
			row.put("id", group.getId());
			row.put("description", group.getDescription());
			final String userDefaultGroupName = user.getDefaultGroupName();
			if (userDefaultGroupName != null && userDefaultGroupName.equalsIgnoreCase(group.getName())) {
				row.put("isdefault", true);
			} else {
				row.put("isdefault", false);
			}
			jsonGroupList.put(row);
		}
		return jsonGroupList;
	}

	public static JSONObject serialize(final CMUser user) throws JSONException {
		final JSONObject row = new JSONObject();
		row.put(USER_ID, user.getId());
		row.put(USER_NAME, user.getUsername());
		row.put(DESCRIPTION, user.getDescription());
		row.put(EMAIL, user.getEmail());
		row.put(IS_ACTIVE, user.isActive());
		row.put(SERVICE, user.isService());
		row.put(PRIVILEGED, user.isPrivileged());
		return row;
	}

	public static JSONArray serializeUsers(final Iterable<CMUser> users) throws JSONException {
		final JSONArray userList = new JSONArray();
		for (final CMUser user : users) {
			userList.put(Serializer.serialize(user));
		}
		return userList;
	}

	public static void addAttachmentsData(final JSONObject jsonTable, final CMClass cmClass, final DmsLogic dmsLogic,
			final Notifier notifier) throws JSONException {
		final DmsConfiguration dmsConfiguration = applicationContext().getBean(DmsConfiguration.class);
		if (!dmsConfiguration.isEnabled()) {
			return;
		}
		final Map<String, Map<String, String>> rulesByGroup = rulesByGroup(cmClass, dmsLogic, notifier);

		final JSONObject jsonGroups = new JSONObject();
		for (final String groupName : rulesByGroup.keySet()) {
			jsonGroups.put(groupName, rulesByGroup.get(groupName));
		}

		final JSONObject jsonAutocompletion = new JSONObject();
		jsonAutocompletion.put("autocompletion", jsonGroups);
		try {
			final JSONObject jsonMeta = jsonTable.getJSONObject(META);
			jsonMeta.put("attachments", jsonAutocompletion);
		} catch (final JSONException ex) {
			// there is no meta key
		}
	}

	private static Map<String, Map<String, String>> rulesByGroup(final CMClass cmClass, final DmsLogic dmsLogic,
			final Notifier notifier) {
		try {
			return dmsLogic.getAutoCompletionRulesByClass(cmClass.getIdentifier().getLocalName());
		} catch (final DmsException e) {
			notifier.warn(e);
			return Collections.emptyMap();
		}
	}

}
