package org.cmdbuild.servlets.json.util;

import static com.google.common.collect.FluentIterable.from;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.data.store.lookup.Functions.toLookupId;
import static org.cmdbuild.logic.mapping.json.Constants.FilterOperator.IN;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.ATTRIBUTE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.OPERATOR_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.VALUE_KEY;
import static org.cmdbuild.workflow.ProcessAttributes.FlowStatus;

import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.logic.mapping.json.JsonFilterHelper.FilterElementGetter;
import org.cmdbuild.workflow.LookupHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Optional;

public class FlowStatusFilterElementGetter implements FilterElementGetter {

	private final LookupHelper lookupHelper;
	private final String flowStatus;

	public FlowStatusFilterElementGetter(final LookupHelper lookupHelper, final String flowStatus) {
		this.lookupHelper = lookupHelper;
		this.flowStatus = flowStatus;
	}

	@Override
	public boolean hasElement() {
		return !isBlank(flowStatus);
	}

	@Override
	public JSONObject getElement() throws JSONException {
		logger.debug(marker, "creating JSON flow status element for '{}'", flowStatus);
		final Optional<Lookup> lookup = lookupHelper.flowStatusWithCode(flowStatus);
		final Iterable<Long> ids;
		if (lookup.isPresent()) {
			ids = from(asList(lookup.get())).transform(toLookupId());
		} else {
			final Iterable<Lookup> allLookups = lookupHelper.allLookups();
			ids = from(allLookups).transform(toLookupId());
		}
		final JSONArray values = new JSONArray();
		for (final Long id : ids) {
			values.put(id);
		}
		final JSONObject simple;
		simple = new JSONObject();
		simple.put(ATTRIBUTE_KEY, FlowStatus.dbColumnName());
		simple.put(OPERATOR_KEY, IN);
		simple.put(VALUE_KEY, values);

		logger.debug(marker, "resulting element is '{}'", simple);

		return simple;
	}
}
