package org.cmdbuild.service.rest.v1.cxf.util;

import static org.apache.commons.lang3.StringUtils.isBlank;

import org.cmdbuild.service.rest.v1.logging.LoggingSupport;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Json implements LoggingSupport {

	public static JSONObject safeJsonObject(final String json) {
		try {
			return isBlank(json) ? new JSONObject() : new JSONObject(json);
		} catch (final JSONException e) {
			logger.error("error parsing json", e);
			throw new IllegalArgumentException(e);
		}
	}

	public static JSONArray safeJsonArray(final String json) {
		try {
			return isBlank(json) ? new JSONArray() : new JSONArray(json);
		} catch (final JSONException e) {
			logger.error("error parsing json", e);
			throw new IllegalArgumentException(e);
		}
	}

	private Json() {
		// prevents instantiation
	}

}
