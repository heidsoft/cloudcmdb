package org.cmdbuild.servlets.json.serializers;

import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.GROUPS;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.QUERY;
import static org.cmdbuild.servlets.json.CommunicationConstants.TITLE;
import static org.cmdbuild.servlets.json.CommunicationConstants.TYPE;

import org.cmdbuild.services.store.report.Report;
import org.json.JSONException;
import org.json.JSONObject;

public class ReportSerializer {

	public JSONObject toClient(final Report report) throws JSONException {

		final JSONObject serializer = new JSONObject();
		serializer.put(ID, report.getId());
		serializer.put(TITLE, report.getCode());
		serializer.put(DESCRIPTION, report.getDescription());
		serializer.put(TYPE, report.getType());
		serializer.put(QUERY, report.getQuery());
		serializer.put(GROUPS, report.getGroups());
		return serializer;
	}
}
