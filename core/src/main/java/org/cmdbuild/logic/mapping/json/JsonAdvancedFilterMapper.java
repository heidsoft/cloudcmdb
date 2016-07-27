package org.cmdbuild.logic.mapping.json;

import static com.google.common.base.Functions.identity;
import static com.google.common.collect.FluentIterable.from;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.common.utils.guava.Functions.build;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Id;
import static org.cmdbuild.logic.mapping.json.Constants.FilterOperator.IN;
import static org.cmdbuild.logic.mapping.json.Constants.FilterOperator.NULL;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.ATTRIBUTE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.CLASSNAME_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.FULL_TEXT_QUERY_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.FUNCTION_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.OPERATOR_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_CARDS_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_CARD_ID_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_DESTINATION_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_DOMAIN_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_SOURCE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_TYPE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_TYPE_NOONE;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_TYPE_ONEOF;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.SIMPLE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.VALUE_KEY;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.mapping.FilterMapper;
import org.cmdbuild.logic.validation.Validator;
import org.cmdbuild.logic.validation.json.JsonFilterValidator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class JsonAdvancedFilterMapper implements FilterMapper {

	private static final Logger logger = Log.CMDBUILD;
	private static final Marker marker = MarkerFactory.getMarker(JsonAdvancedFilterMapper.class.getName());

	private static final JSONArray EMPTY_VALUES = new JSONArray();

	public static class Builder implements org.apache.commons.lang3.builder.Builder<JsonAdvancedFilterMapper> {

		private static final Function<WhereClause, WhereClause> IDENTITY = identity();

		private CMEntryType entryType;
		private JSONObject filterObject;
		private CMDataView dataView;
		private Alias entryTypeAlias;
		private OperationUser operationUser;
		private Function<WhereClause, WhereClause> function;

		private Builder() {
			// use factory method
		}

		@Override
		public JsonAdvancedFilterMapper build() {
			validate();
			return new JsonAdvancedFilterMapper(this);
		}

		private void validate() {
			Validate.notNull(entryType);
			Validate.notNull(filterObject);
			function = defaultIfNull(function, IDENTITY);
		}

		public Builder withEntryType(final CMEntryType entryType) {
			this.entryType = entryType;
			return this;
		}

		public Builder withFilterObject(final JSONObject filterObject) {
			this.filterObject = filterObject;
			return this;
		}

		public Builder withDataView(final CMDataView dataView) {
			this.dataView = dataView;
			return this;
		}

		public Builder withEntryTypeAlias(final Alias entryTypeAlias) {
			this.entryTypeAlias = entryTypeAlias;
			return this;
		}

		public Builder withOperationUser(final OperationUser operationUser) {
			this.operationUser = operationUser;
			return this;
		}

		public Builder withFunction(final Function<WhereClause, WhereClause> function) {
			this.function = function;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private static final Function<org.apache.commons.lang3.builder.Builder<? extends WhereClause>, WhereClause> BUILD_WHERE_CLAUSES = build();

	private final CMEntryType entryType;
	private final JSONObject filterObject;
	private final Validator filterValidator;
	private final CMDataView dataView;
	private final Alias entryTypeAlias;
	private final OperationUser operationUser;
	private final Function<WhereClause, WhereClause> function;

	private JsonAdvancedFilterMapper(final Builder builder) {
		this.entryType = builder.entryType;
		this.filterObject = builder.filterObject;
		this.filterValidator = new JsonFilterValidator(builder.filterObject);
		this.dataView = builder.dataView;
		this.entryTypeAlias = builder.entryTypeAlias;
		this.operationUser = builder.operationUser;
		this.function = builder.function;
	}

	@Override
	public CMEntryType entryType() {
		logger.info(marker, "getting entry type");
		return entryType;
	}

	@Override
	public Iterable<WhereClause> whereClauses() {
		logger.info(marker, "getting where clause");
		filterValidator.validate();
		try {
			return from(getWhereClauseBuildersForFilter()) //
					.transform(BUILD_WHERE_CLAUSES);
		} catch (final JSONException ex) {
			throw new IllegalArgumentException("malformed filter", ex);
		}
	}

	private Iterable<org.cmdbuild.common.Builder<WhereClause>> getWhereClauseBuildersForFilter() throws JSONException {
		final List<org.cmdbuild.common.Builder<WhereClause>> whereClauseBuilders = Lists.newArrayList();
		if (filterObject.has(ATTRIBUTE_KEY)) {
			whereClauseBuilders.add(JsonAttributeFilterBuilder.newInstance() //
					.withFilterObject(filterObject.getJSONObject(ATTRIBUTE_KEY)) //
					.withEntryType(entryType) //
					.withEntryTypeAlias(entryTypeAlias) //
					.withDataView(dataView) //
					.withFunction(function));
		}
		if (filterObject.has(FULL_TEXT_QUERY_KEY)) {
			whereClauseBuilders.add(JsonFullTextQueryBuilder.newInstance() //
					.withFullTextQuery(filterObject.getString(FULL_TEXT_QUERY_KEY)) //
					.withEntryType(entryType) //
					.withEntryTypeAlias(entryTypeAlias));
		}
		if (filterObject.has(RELATION_KEY)) {
			final JSONArray conditions = filterObject.getJSONArray(RELATION_KEY);
			for (int i = 0; i < conditions.length(); i++) {
				final JSONObject condition = conditions.getJSONObject(i);
				if (condition.getString(RELATION_TYPE_KEY).equals(RELATION_TYPE_ONEOF)) {
					final JSONArray cards = condition.getJSONArray(RELATION_CARDS_KEY);

					final JSONObject simple = new JSONObject();
					simple.put(ATTRIBUTE_KEY, Id.getDBName());
					simple.put(OPERATOR_KEY, IN.toString());
					simple.put(CLASSNAME_KEY, condition.getString(RELATION_DESTINATION_KEY));

					final JSONObject filter = new JSONObject();
					filter.put(SIMPLE_KEY, simple);

					for (int j = 0; j < cards.length(); j++) {
						final JSONObject card = cards.getJSONObject(j);
						final Long id = card.getLong(RELATION_CARD_ID_KEY);
						simple.append(VALUE_KEY, id);
					}

					whereClauseBuilders.add(JsonAttributeFilterBuilder.newInstance() //
							.withFilterObject(filter) //
							.withEntryType(entryType) //
							.withEntryTypeAlias(entryTypeAlias) //
							.withDataView(dataView));
				} else if (condition.getString(RELATION_TYPE_KEY).equals(RELATION_TYPE_NOONE)) {
					final JSONObject simple = new JSONObject();
					simple.put(ATTRIBUTE_KEY, Id.getDBName());
					simple.put(OPERATOR_KEY, NULL.toString());
					simple.put(CLASSNAME_KEY, condition.getString(RELATION_DESTINATION_KEY));
					simple.put(VALUE_KEY, EMPTY_VALUES);

					final JSONObject filter = new JSONObject();
					filter.put(SIMPLE_KEY, simple);

					whereClauseBuilders.add(JsonAttributeFilterBuilder.newInstance() //
							.withFilterObject(filter) //
							.withEntryType(entryType) //
							.withEntryTypeAlias(entryTypeAlias) //
							.withDataView(dataView));

				}
			}
		}
		if (filterObject.has(FUNCTION_KEY)) {
			final JSONArray array = filterObject.getJSONArray(FUNCTION_KEY);
			for (int i = 0; i < array.length(); i++) {
				final JSONObject definition = array.getJSONObject(i);
				whereClauseBuilders.add(JsonFunctionFilterBuilder.newInstance() //
						.withFilterObject(definition) //
						.withEntryType(entryType) //
						.withEntryTypeAlias(entryTypeAlias) //
						.withOperationUser(operationUser));
			}
		}
		return whereClauseBuilders;
	}

	@Override
	public Iterable<JoinElement> joinElements() {
		logger.info(marker, "getting join elements");
		final List<JoinElement> joinElements = Lists.newArrayList();
		if (filterObject.has(RELATION_KEY)) {
			try {
				final JSONArray conditions = filterObject.getJSONArray(RELATION_KEY);
				for (int i = 0; i < conditions.length(); i++) {
					final JSONObject condition = conditions.getJSONObject(i);
					final String domain = condition.getString(RELATION_DOMAIN_KEY);
					final String source = condition.getString(RELATION_SOURCE_KEY);
					final String destination = condition.getString(RELATION_DESTINATION_KEY);
					final boolean left = condition.getString(RELATION_TYPE_KEY).equals(RELATION_TYPE_NOONE);
					joinElements.add(JoinElement.newInstance(domain, source, destination, left));
				}
			} catch (final Exception e) {
				logger.error(marker, "error getting json element", e);
			}
		}
		return joinElements;
	}

}
