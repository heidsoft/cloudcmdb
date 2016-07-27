package org.cmdbuild.logic.mapping.json;

import static java.util.Arrays.asList;
import static org.cmdbuild.logic.mapping.json.Constants.FilterOperator.EQUAL;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.ATTRIBUTE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.OPERATOR_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.VALUE_KEY;

import org.cmdbuild.logic.mapping.json.JsonFilterHelper.FilterElementGetter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FilterElementGetters {

	private static class IdFilterElementGetter implements FilterElementGetter {

		private final Long id;

		public IdFilterElementGetter(final Long id) {
			this.id = id;
		}

		@Override
		public boolean hasElement() {
			return true;
		}

		@Override
		public JSONObject getElement() throws JSONException {
			logger.debug(marker, "creating JSON element for '{}'", id);
			final JSONObject element = new JSONObject();
			element.put(ATTRIBUTE_KEY, "Id");
			element.put(OPERATOR_KEY, EQUAL);
			element.put(VALUE_KEY, new JSONArray(asList(id)));
			logger.debug(marker, "resulting element is '{}'", element);
			return element;
		}

	}

	public static FilterElementGetter id(final Long value) {
		return new IdFilterElementGetter(value);
	}

	private FilterElementGetters() {
		// prevents instantiation
	}

}
