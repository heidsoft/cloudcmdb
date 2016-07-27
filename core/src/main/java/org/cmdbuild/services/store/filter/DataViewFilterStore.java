package org.cmdbuild.services.store.filter;

import static com.google.common.collect.FluentIterable.from;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.common.Constants.ID_ATTRIBUTE;
import static org.cmdbuild.common.Constants.ROLE_CLASS_NAME;
import static org.cmdbuild.dao.entry.Functions.toAttributeValue;
import static org.cmdbuild.dao.guava.Functions.toCard;
import static org.cmdbuild.dao.guava.Functions.toRelation;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.OrderByClause.Direction.ASC;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Aliases.as;
import static org.cmdbuild.dao.query.clause.alias.Aliases.name;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.OperatorAndValues.eq;
import static org.cmdbuild.dao.query.clause.where.OperatorAndValues.in;
import static org.cmdbuild.dao.query.clause.where.WhereClauses.alwaysFalse;
import static org.cmdbuild.dao.query.clause.where.WhereClauses.alwaysTrue;
import static org.cmdbuild.dao.query.clause.where.WhereClauses.and;
import static org.cmdbuild.dao.query.clause.where.WhereClauses.condition;
import static org.cmdbuild.dao.query.clause.where.WhereClauses.not;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.dao.StorableConverter;
import org.cmdbuild.privileges.GrantCleaner;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

public class DataViewFilterStore implements FilterStore {

	public static final String CLASS_NAME = "_Filter";
	private static final String DOMAIN_NAME = "FilterRole";

	public static final String ID = ID_ATTRIBUTE;
	public static final String NAME = CODE_ATTRIBUTE;
	public static final String DESCRIPTION = DESCRIPTION_ATTRIBUTE;
	public static final String CLASS_ID = "ClassId";
	public static final String FILTER = "Filter";
	public static final String SHARED = "Shared";
	public static final String USER_ID = "UserId";

	private static final Alias F = name("f");
	private static final Alias R = name("r");
	private static final Alias D = name("d");

	private static WhereClause forUser(final Long id) {
		final WhereClause clause;
		if (id == null) {
			clause = alwaysTrue();
		} else {
			clause = condition(attribute(F, USER_ID), eq(id));
		}
		return clause;
	}

	private static WhereClause isShared() {
		return condition(attribute(F, SHARED), eq(true));
	}

	private static WhereClause forRole(final String name) {
		final WhereClause clause;
		if (isBlank(name)) {
			clause = alwaysTrue();
		} else {
			clause = condition(attribute(R, CODE_ATTRIBUTE), eq(name));
		}
		return clause;
	}

	private static WhereClause forClass(final CMEntryType entryType) {
		final WhereClause clause;
		if (entryType == null) {
			clause = alwaysTrue();
		} else {
			clause = condition(attribute(F, CLASS_ID), eq(entryType.getId()));
		}
		return clause;
	}

	private WhereClause filterIds(final Long... ids) {
		final WhereClause clause;
		if (ids == null) {
			clause = alwaysTrue();
		} else if (ids.length == 0) {
			clause = alwaysFalse();
		} else {
			clause = condition(attribute(F, ID), in(ids));
		}
		return clause;
	}

	private final CMDataView view;
	private final GrantCleaner grantCleaner;
	private final StorableConverter<FilterStore.Filter> converter;

	public DataViewFilterStore(final CMDataView dataView, final StorableConverter<FilterStore.Filter> converter) {
		this.view = dataView;
		this.grantCleaner = new GrantCleaner(view);
		this.converter = converter;
	}

	@Override
	public Filter read(final Long filterId) {
		return cardToFilter().apply(filterCard(filterId));
	}

	@Override
	public PagedElements<Filter> readNonSharedFilters(final String className, final Long userId, final int offset,
			final int limit) {
		logger.debug("getting user filters for class '{}' starting from '{}' with a limit of '{}'", className, offset,
				limit);
		final CMQueryResult result = view.select(anyAttribute(F)) //
				.from(filterClass(), as(F)) //
				.where(and(not(isShared()), forUser(userId), forClass(className))) //
				.offset(offset) //
				.limit(limit) //
				.orderBy(attribute(F, NAME), ASC) //
				.count() //
				.run();
		final Iterable<Filter> filters = from(result) //
				.transform(toCard(F)) //
				.transform(cardToFilter());
		return new PagedElements<Filter>(filters, result.totalSize());
	}

	@Override
	public PagedElements<Filter> readSharedFilters(final String className, final int start, final int limit) {
		logger.debug("getting all filter cards");
		final CMQueryResult result = view.select(anyAttribute(F)) //
				.from(filterClass(), as(F)) //
				.where(and(isShared(), forClass(className))) //
				.offset(start) //
				.limit(limit) //
				.orderBy(attribute(F, NAME), ASC) //
				.count() //
				.run();
		final Iterable<Filter> filters = from(result) //
				.transform(toCard(F)) //
				.transform(cardToFilter());
		return new PagedElements<Filter>(filters, result.totalSize());
	}

	@Override
	public Long create(final Filter filter) {
		return converter.fill(view.createCardFor(filterClass()), filter) //
				.save() //
				.getId();
	}

	@Override
	public void update(final Filter filter) {
		converter.fill(view.update(filterCard(filter.getId())), filter) //
				.save();
	}

	@Override
	public void delete(final Filter filter) {
		view.delete(filterCard(filter.getId()));
		grantCleaner.deleteGrantReferingTo(filter.getId());
	}

	@Override
	public FluentIterable<Filter> read(final String className, final String groupName) {
		final CMQueryResult result = view.select(anyAttribute(F), anyAttribute(R)) //
				.from(filterClass(), as(F)) //
				.join(roleClass(), as(R), over(filterRoleDomain(), as(D))) //
				.where(and(forClass(className), forRole(groupName))) //
				.orderBy(attribute(F, NAME), ASC) //
				.run();
		return from(result) //
				.transform(toCard(F)) //
				.transform(cardToFilter());
	}

	@Override
	public void join(final String groupName, final Iterable<Filter> filters) {
		for (final Filter element : filters) {
			view.createRelationFor(filterRoleDomain()) //
					.setCard1(filterCard(element)) //
					.setCard2(roleCard(groupName)) //
					.create();
		}
	}

	@Override
	public void disjoin(final String groupName, final Iterable<Filter> filters) {
		final Long[] ids = from(filters) //
				.transform(new Function<Filter, Long>() {

					@Override
					public Long apply(final Filter input) {
						return input.getId();
					}

				}) //
				.toArray(Long.class);
		final CMQueryResult result = view.select(anyAttribute(F), anyAttribute(R)) //
				.from(filterClass(), as(F)) //
				.join(roleClass(), as(R), over(filterRoleDomain(), as(D))) //
				.where(and(filterIds(ids), forRole(groupName))) //
				.orderBy(attribute(F, NAME), ASC) //
				.run();
		for (final CMRelation relation : from(result).transform(toRelation(D))) {
			view.delete(relation);
		}
	}

	@Override
	public Iterable<String> joined(final Long filter) {
		final CMQueryResult result = view.select(anyAttribute(F), anyAttribute(R)) //
				.from(filterClass(), as(F)) //
				.join(roleClass(), as(R), over(filterRoleDomain(), as(D))) //
				.where(filterIds(filter)) //
				.orderBy(attribute(F, NAME), ASC) //
				.run();
		return from(result) //
				.transform(toCard(R)) //
				.transform(toAttributeValue(CODE_ATTRIBUTE, String.class));
	}

	/*
	 * Utilities
	 */

	private CMClass filterClass() {
		return view.findClass(CLASS_NAME);
	}

	private CMClass roleClass() {
		return view.findClass(ROLE_CLASS_NAME);
	}

	private CMDomain filterRoleDomain() {
		return view.findDomain(DOMAIN_NAME);
	}

	private CMCard filterCard(final Filter filter) {
		return filterCard(filter.getId());
	}

	private CMCard filterCard(final Long id) {
		logger.debug("getting filter card with id '{}'", id);
		return view.select(anyAttribute(F)) //
				.from(filterClass(), as(F)) //
				.where(condition(attribute(F, ID), eq(id))) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow() //
				.getCard(F);
	}

	private CMCard roleCard(final String name) {
		logger.debug("getting role card with name '{}'", name);
		return view.select(anyAttribute(R)) //
				.from(roleClass(), as(R)) //
				.where(forRole(name)) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow() //
				.getCard(R);
	}

	private WhereClause forClass(final String name) {
		final WhereClause clause;
		if (name == null) {
			clause = alwaysTrue();
		} else {
			final CMClass entryType = view.findClass(name);
			clause = forClass(entryType);
		}
		return clause;
	}

	private Function<CMCard, Filter> cardToFilter() {
		return new Function<CMCard, Filter>() {

			@Override
			public Filter apply(final CMCard input) {
				return converter.convert(input);
			}

		};
	}

}
