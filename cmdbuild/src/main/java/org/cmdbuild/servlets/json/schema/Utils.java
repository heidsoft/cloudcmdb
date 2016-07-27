package org.cmdbuild.servlets.json.schema;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.List;
import java.util.Map;

import org.cmdbuild.logger.Log;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Utils {

	private static final Logger logger = Log.JSONRPC;
	private static final Marker marker = MarkerFactory.getMarker(Utils.class.getName());

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private Utils() {
		// prevents instantiation
	}

	public static Map<String, String> toMap(final JSONObject json) {
		try {
			final Map<String, String> map = Maps.newHashMap();
			if (json != null && json.length() > 0) {
				for (final String key : JSONObject.getNames(json)) {
					map.put(key, json.getString(key));
				}
			}
			return map;
		} catch (final Exception e) {
			logger.warn(marker, "error parsing json data");
			throw new RuntimeException(e);
		}
	}

	public static enum JsonParser {

		AS_STRING {

			@Override
			public Object serialize(final JSONArray json, final int index) throws JSONException {
				return json.getString(index);
			}

		}, //
		AS_LONG {

			@Override
			public Object serialize(final JSONArray json, final int index) throws JSONException {
				return json.getLong(index);
			}

		}, //
		DEFAULT {

			@Override
			public Object serialize(final JSONArray json, final int index) throws JSONException {
				return AS_STRING.serialize(json, index);
			}

		}, //
		;

		abstract Object serialize(JSONArray json, int index) throws JSONException;

	}

	public static <T> Iterable<T> toIterable(final String json) {
		try {
			return OBJECT_MAPPER.readValue(json, new TypeReference<List<T>>() {
			});
		} catch (final Exception e) {
			logger.warn(marker, "error parsing json data");
			throw new RuntimeException(e);
		}
	}

	public static Iterable<String> toIterable(final JSONArray json) {
		return toIterable(json, JsonParser.DEFAULT);
	}

	public static <T> Iterable<T> toIterable(final JSONArray json, final JsonParser parser) {
		try {
			final List<T> values = Lists.newArrayList();
			if (json != null && json.length() > 0) {
				for (int index = 0; index < json.length(); index++) {
					final T value = (T) defaultIfNull(parser, JsonParser.DEFAULT).serialize(json, index);
					values.add(value);
				}
			}
			return values;
		} catch (final Exception e) {
			logger.warn(marker, "error parsing json data");
			throw new RuntimeException(e);
		}
	}

	public static JSONArray toJsonArray(final Iterable<? extends Object> elements) {
		final JSONArray json = new JSONArray();
		for (final Object element : elements) {
			json.put(element);
		}
		return json;
	}

}
