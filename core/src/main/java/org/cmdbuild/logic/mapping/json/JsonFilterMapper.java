package org.cmdbuild.logic.mapping.json;

import static com.google.common.base.Functions.identity;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.CQL_KEY;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.mapping.FilterMapper;
import org.cmdbuild.logic.mapping.ForwardingFilterMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;

public class JsonFilterMapper extends ForwardingFilterMapper {

	private static final Logger logger = Log.CMDBUILD;

	public static class Builder implements org.apache.commons.lang3.builder.Builder<JsonFilterMapper> {

		private static final Marker marker = MarkerFactory.getMarker(Builder.class.getName());

		private static final Function<WhereClause, WhereClause> IDENTITY = identity();

		private CMDataView dataView;
		private CMEntryType entryType;
		private Alias entryTypeAlias;
		private JSONObject filterObject;
		private OperationUser operationUser;
		private Function<WhereClause, WhereClause> function;

		private Builder() {
			// use factory method
		}

		@Override
		public JsonFilterMapper build() {
			validate();
			return new JsonFilterMapper(this);
		}

		private void validate() {
			Validate.notNull(entryType);
			Validate.notNull(filterObject);
			if (filterObject.has(CQL_KEY)) {
				logger.error(marker, "cannot use this when there is CQL");
				throw new UnsupportedOperationException();
			}
			function = defaultIfNull(function, IDENTITY);
		}

		public Builder withDataView(final CMDataView dataView) {
			this.dataView = dataView;
			return this;
		}

		public Builder withEntryType(final CMEntryType entryType) {
			this.entryType = entryType;
			return this;
		}

		public Builder withEntryTypeAlias(final Alias entryTypeAlias) {
			this.entryTypeAlias = entryTypeAlias;
			return this;
		}

		public Builder withFilterObject(final JSONObject filterObject) {
			this.filterObject = filterObject;
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

	private final FilterMapper delegate;

	private JsonFilterMapper(final Builder builder) {
		this.delegate = JsonAdvancedFilterMapper.newInstance() //
				.withEntryType(builder.entryType) //
				.withFilterObject(builder.filterObject) //
				.withDataView(builder.dataView) //
				.withEntryTypeAlias(builder.entryTypeAlias) //
				.withOperationUser(builder.operationUser) //
				.withFunction(builder.function) //
				.build();
	}

	@Override
	protected FilterMapper delegate() {
		return delegate;
	}

}
