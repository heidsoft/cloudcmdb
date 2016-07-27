package org.cmdbuild.servlets.json.serializers;

import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILTER;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.SOURCE_CLASS_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.SOURCE_FUNCTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.TYPE;
import static org.cmdbuild.servlets.json.CommunicationConstants.VIEWS;

import java.util.List;

import org.cmdbuild.model.view.View;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ViewSerializer {

	public JSONObject toClient(final List<View> views) throws JSONException {
		final JSONObject out = new JSONObject();
		final JSONArray jsonViews = new JSONArray();

		for (final View view : views) {
			jsonViews.put(toClient(view));
		}

		out.put(VIEWS, jsonViews);

		return out;
	}

	public JSONObject toClient(final View view) throws JSONException {
		final JSONObject jsonView = new JSONObject();
		jsonView.put(DESCRIPTION, view.getDescription());
		jsonView.put(FILTER, view.getFilter());
		jsonView.put(ID, view.getId());
		jsonView.put(NAME, view.getName());
		jsonView.put(SOURCE_CLASS_NAME, view.getSourceClassName());
		jsonView.put(SOURCE_FUNCTION, view.getSourceFunction());
		jsonView.put(TYPE, view.getType().toString());
		return jsonView;
	}
}
