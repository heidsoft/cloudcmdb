package org.cmdbuild.logic.mapping.json;

import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.WhereClauses.function;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.FUNCTION_NAME_KEY;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.Builder;
import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonFunctionFilterBuilder implements Builder<WhereClause> {

	public static JsonFunctionFilterBuilder newInstance() {
		return new JsonFunctionFilterBuilder();
	}

	private static final String ATTRIBUTE_NAME = SystemAttributes.Id.getDBName();

	private JSONObject filterObject;
	private CMEntryType entryType;
	private Alias entryTypeAlias;
	private OperationUser operationUser;

	private JsonFunctionFilterBuilder() {
		// use factory method
	}

	public JsonFunctionFilterBuilder withFilterObject(final JSONObject filterObject) {
		this.filterObject = filterObject;
		return this;
	}

	public JsonFunctionFilterBuilder withEntryType(final CMEntryType entryType) {
		this.entryType = entryType;
		return this;
	}

	public JsonFunctionFilterBuilder withEntryTypeAlias(final Alias entryTypeAlias) {
		this.entryTypeAlias = entryTypeAlias;
		return this;
	}

	public JsonFunctionFilterBuilder withOperationUser(final OperationUser operationUser) {
		this.operationUser = operationUser;
		return this;
	}

	@Override
	public WhereClause build() {
		validate();
		return doBuild();
	}

	private void validate() {
		Validate.notNull(filterObject, "invalid filter");
		Validate.notNull(entryType, "invalid entry type");
	}

	private WhereClause doBuild() {
		try {
			final CMEntryType entryType = this.entryType;
			if (filterObject.has(FUNCTION_NAME_KEY)) {
				final String name = filterObject.getString(FUNCTION_NAME_KEY);
				final Long userId = (operationUser == null) ? null : operationUser.getAuthenticatedUser().getId();
				final Long roleId = (operationUser == null) ? null : operationUser.getPreferredGroup().getId();
				final QueryAliasAttribute attribute = (entryTypeAlias == null) ? attribute(entryType, ATTRIBUTE_NAME)
						: attribute(entryTypeAlias, ATTRIBUTE_NAME);
				return function(attribute, name, userId, roleId, entryType);
			}
			throw new IllegalArgumentException("The filter is malformed");
		} catch (final JSONException e) {
			throw new IllegalArgumentException("malformed filter", e);
		}
	}

}
