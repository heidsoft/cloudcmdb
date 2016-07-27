package org.cmdbuild.logic.mapping.json;

import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Aliases.name;
import static org.cmdbuild.dao.query.clause.where.ContainsOperatorAndValue.contains;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.OrWhereClause.or;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.Builder;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForwardingAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.query.ExternalReferenceAliasHandler;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.where.EmptyWhereClause;
import org.cmdbuild.dao.query.clause.where.OperatorAndValue;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;

import com.google.common.collect.Lists;

/**
 * Creates a WhereClause starting from a full text query filter. This means that
 * it searches if the text is in almost one of all the attributes of the
 * specified class
 */
public class JsonFullTextQueryBuilder implements Builder<WhereClause> {

	public static JsonFullTextQueryBuilder newInstance() {
		return new JsonFullTextQueryBuilder();
	}

	private String fullTextQuery;
	private CMEntryType entryType;
	private Alias entryTypeAlias;

	private JsonFullTextQueryBuilder() {
		// use factory method
	}

	public JsonFullTextQueryBuilder withFullTextQuery(final String fullTextQuery) {
		this.fullTextQuery = fullTextQuery;
		return this;
	}

	public JsonFullTextQueryBuilder withEntryType(final CMEntryType entryType) {
		this.entryType = entryType;
		return this;
	}

	public JsonFullTextQueryBuilder withEntryTypeAlias(final Alias entryTypeAlias) {
		this.entryTypeAlias = entryTypeAlias;
		return this;
	}

	@Override
	public WhereClause build() {
		validate();
		return doBuild();
	}

	private void validate() {
		Validate.notNull(fullTextQuery, "missing full-text query");
		Validate.notNull(entryType, "missing entry type");
	}

	private WhereClause doBuild() {
		final List<WhereClause> whereClauses = Lists.newArrayList();
		for (final CMAttribute attribute : entryType.getActiveAttributes()) {
			final OperatorAndValue opAndVal = contains(fullTextQuery);
			final QueryAliasAttribute aliasAttribute;
			if (!isExternalReferenceAttribute(attribute)) {
				if (entryTypeAlias == null) {
					aliasAttribute = attribute(entryType, attribute.getName());
				} else {
					aliasAttribute = attribute(entryTypeAlias, attribute.getName());
				}
			} else {
				final Alias lookupClassAlias = name(new ExternalReferenceAliasHandler(entryType, attribute).forQuery());
				aliasAttribute = attribute(lookupClassAlias, ExternalReferenceAliasHandler.EXTERNAL_ATTRIBUTE);
			}
			final SimpleWhereClause simpleWhereClause = (SimpleWhereClause) condition(aliasAttribute, opAndVal);
			simpleWhereClause.setAttributeNameCast("varchar");
			whereClauses.add(simpleWhereClause);
		}

		final WhereClause[] whereClausesArray = whereClauses.toArray(new WhereClause[whereClauses.size()]);
		if (whereClauses.isEmpty()) {
			return EmptyWhereClause.emptyWhereClause();
		}
		if (whereClauses.size() == 1) {
			return whereClauses.get(0);
		} else if (whereClauses.size() == 2) {
			return or(whereClausesArray[0], whereClausesArray[1]);
		} else {
			return or(whereClausesArray[0], whereClausesArray[1],
					Arrays.copyOfRange(whereClausesArray, 2, whereClausesArray.length));
		}
	}

	private boolean isExternalReferenceAttribute(final CMAttribute attribute) {
		final CMAttributeType<?> attributeType = attribute.getType();
		return attributeType instanceof LookupAttributeType || //
				attributeType instanceof ReferenceAttributeType || //
				attributeType instanceof ForeignKeyAttributeType;
	}

	/**
	 * This class is a visitor for an attribute type. Its main function is to
	 * return an OperationAndValue object (where possible), according to the
	 * specific type of the attribute. Created because it is not possible to use
	 * a generic 'contains' OperatorAndValue(e.g. integer or inet attributes)
	 */
	public static class FullTextQueryOperatorVisitor extends ForwardingAttributeTypeVisitor {

		private static final CMAttributeTypeVisitor DELEGATE = NullAttributeTypeVisitor.getInstance();

		private OperatorAndValue operatorAndValue;
		private final CMAttributeType<?> type;
		private final String fullText;

		public FullTextQueryOperatorVisitor(final CMAttributeType<?> type, final String fullText) {
			this.type = type;
			this.fullText = fullText;
			this.operatorAndValue = null;
		}

		@Override
		protected CMAttributeTypeVisitor delegate() {
			return DELEGATE;
		}

		@Override
		public void visit(final BooleanAttributeType attributeType) {
			if (isValidBoolean(fullText)) {
				final boolean castValue = Boolean.valueOf(fullText);
				operatorAndValue = eq(castValue);
			}
		}

		private static boolean isValidBoolean(final String str) {
			if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false")) {
				return true;
			}
			return false;
		}

		@Override
		public void visit(final CharAttributeType attributeType) {
			operatorAndValue = eq(fullText);
		}

		@Override
		public void visit(final DateAttributeType attributeType) {
			final Object convertedValue = attributeType.convertValue(fullText);
			if (convertedValue != null) {
				operatorAndValue = eq(convertedValue);
			}
		}

		@Override
		public void visit(final DecimalAttributeType attributeType) {
			if (isNumeric(fullText)) {
				final Double castValue = Double.valueOf(fullText);
				operatorAndValue = eq(castValue);
			}
		}

		@Override
		public void visit(final DoubleAttributeType attributeType) {
			if (isNumeric(fullText)) {
				final Double castValue = Double.valueOf(fullText);
				operatorAndValue = eq(castValue);
			}
		}

		@Override
		public void visit(final IntegerAttributeType attributeType) {
			if (isNumeric(fullText)) {
				final Integer castValue = Integer.valueOf(fullText);
				operatorAndValue = eq(castValue);
			}
		}

		private static boolean isNumeric(final String str) {
			return str.matches("-?\\d+(\\.\\d+)?");
		}

		@Override
		public void visit(final IpAddressAttributeType attributeType) {
			try {
				if (!fullText.isEmpty()) {
					operatorAndValue = eq(attributeType.convertValue(fullText));
				}
			} catch (final IllegalArgumentException ex) {
				// do nothing (operatorAndValue is still null)
			}
		}

		@Override
		public void visit(final StringAttributeType attributeType) {
			operatorAndValue = contains(fullText);
		}

		@Override
		public void visit(final TextAttributeType attributeType) {
			operatorAndValue = contains(fullText);
		}

		public OperatorAndValue getOperatorAndValue() {
			type.accept(this);
			return operatorAndValue;
		}

	}

}
