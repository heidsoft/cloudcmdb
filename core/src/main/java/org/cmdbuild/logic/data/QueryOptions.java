package org.cmdbuild.logic.data;

import static com.google.common.base.Functions.identity;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.transformValues;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.CQL_KEY;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.builder.Builder;
import org.cmdbuild.logger.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

/**
 * Simple DTO that represents the options for a query in CMDBuild
 */
public class QueryOptions {

	private static final Logger logger = Log.CMDBUILD;

	public static class QueryOptionsBuilder implements Builder<QueryOptions> {

		private static final Iterable<String> NO_ATTRIBUTES = emptyList();

		private Integer limit;
		private Integer offset;
		private JSONObject filter;
		private JSONArray sorters;
		private Iterable<String> attributeSubset;
		private Map<String, ? extends Object> parameters;

		private QueryOptionsBuilder() {
			limit = Integer.MAX_VALUE;
			offset = 0;
			filter = new JSONObject();
			sorters = new JSONArray();
			parameters = newHashMap();
		}

		@Override
		public QueryOptions build() {
			validate();
			return new QueryOptions(this);
		}

		private void validate() {
			preReleaseHackToFixCqlFilters();
			offset = defaultIfNull(offset, 0);
			limit = defaultIfNull(limit, Integer.MAX_VALUE);
			attributeSubset = defaultIfNull(attributeSubset, NO_ATTRIBUTES);
		}

		/*
		 * FIXME remove this and fix JavaScript ASAP
		 */
		private void preReleaseHackToFixCqlFilters() {
			try {
				final Map<String, Object> cqlParameters = newHashMap();
				boolean addParameters = false;
				for (final Entry<String, ? extends Object> entry : parameters.entrySet()) {
					final String key = entry.getKey();
					if (key.equals(CQL_KEY)) {
						filter.put(CQL_KEY, entry.getValue());
						addParameters = true;
					} else if (key.startsWith("p")) {
						cqlParameters.put(key, entry.getValue());
					}
				}
				if (addParameters) {
					for (final Entry<String, Object> entry : cqlParameters.entrySet()) {
						filter.put(entry.getKey(), entry.getValue());
					}
				}
			} catch (final Throwable e) {
				logger.error("error while hacking filter", e);
			}
		}

		public QueryOptionsBuilder limit(final Integer limit) {
			this.limit = limit;
			return this;
		}

		public QueryOptionsBuilder offset(final Integer offset) {
			this.offset = offset;
			return this;
		}

		public QueryOptionsBuilder orderBy(final JSONArray sorters) {
			if (sorters == null) {
				this.sorters = new JSONArray();
			} else {
				this.sorters = sorters;
			}
			return this;
		}

		public QueryOptionsBuilder filter(final JSONObject filter) {
			if (filter == null) {
				this.filter = new JSONObject();
			} else {
				this.filter = filter;
			}
			return this;
		}

		public QueryOptionsBuilder onlyAttributes(final Iterable<String> attributes) {
			this.attributeSubset = attributes;
			return this;
		}

		public QueryOptionsBuilder parameters(final Map<String, ? extends Object> parameters) {
			this.parameters = parameters;
			return this;
		}

		public QueryOptionsBuilder clone(final QueryOptions queryOptions) {
			limit = queryOptions.limit;
			offset = queryOptions.offset;
			filter = queryOptions.filter;
			sorters = queryOptions.sorters;
			attributeSubset = queryOptions.attributes;
			parameters = queryOptions.parameters;
			return this;
		}

	}

	public static QueryOptionsBuilder newQueryOption() {
		return new QueryOptionsBuilder();
	}

	private final int limit;
	private final int offset;
	private final JSONObject filter;
	private final JSONArray sorters;
	private final Iterable<String> attributes;
	private final Map<String, Object> parameters;

	private QueryOptions(final QueryOptionsBuilder builder) {
		this.limit = builder.limit;
		this.offset = builder.offset;
		this.filter = builder.filter;
		this.sorters = builder.sorters;
		this.attributes = builder.attributeSubset;
		this.parameters = transformValues(builder.parameters, identity());
	}

	public int getLimit() {
		return limit;
	}

	public int getOffset() {
		return offset;
	}

	public JSONObject getFilter() {
		return filter;
	}

	public JSONArray getSorters() {
		return sorters;
	}

	public Iterable<String> getAttributes() {
		return attributes;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

}
