package org.cmdbuild.data.store.dao;

import static com.google.common.base.Suppliers.memoize;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.dao.driver.postgres.Const.ID_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.dao.query.clause.where.TrueWhereClause.trueWhereClause;
import static org.cmdbuild.data.store.Groupables.notGroupable;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.where.TrueWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.Groupable;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.exception.NotFoundException;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.base.Supplier;

public class DataViewStore<T extends Storable> implements Store<T> {

	protected static final Marker marker = MarkerFactory.getMarker(DataViewStore.class.getName());

	public static class Builder<T extends Storable> implements
			org.apache.commons.lang3.builder.Builder<DataViewStore<T>> {

		private CMDataView dataView;
		private StorableConverter<T> storableConverter;
		private Groupable groupable;

		private Builder() {
			// use factory method
		}

		@Override
		public DataViewStore<T> build() {
			validate();
			return new DataViewStore<T>(this);
		}

		private void validate() {
			Validate.notNull(dataView, "missing '%s'", CMDataView.class);
			Validate.notNull(storableConverter, "missing '%s'", StorableConverter.class);
			storableConverter = wrap(storableConverter);
			groupable = defaultIfNull(groupable, notGroupable());
		}

		private StorableConverter<T> wrap(final StorableConverter<T> converter) {
			return new ForwardingStorableConverter<T>() {

				@Override
				protected StorableConverter<T> delegate() {
					return converter;
				}

				@Override
				public String getIdentifierAttributeName() {
					final String name = super.getIdentifierAttributeName();
					return (name == null) ? DEFAULT_IDENTIFIER_ATTRIBUTE_NAME : name;
				}

			};
		}

		public Builder<T> withDataView(final CMDataView dataView) {
			this.dataView = dataView;
			return this;
		}

		public Builder<T> withStorableConverter(final StorableConverter<T> storableConverter) {
			this.storableConverter = storableConverter;
			return this;
		}

		public Builder<T> withGroupable(final Groupable groupable) {
			this.groupable = groupable;
			return this;
		}

	}

	static final String DEFAULT_IDENTIFIER_ATTRIBUTE_NAME = ID_ATTRIBUTE;

	/**
	 * @deprecated Use {@link newInstance()} instead.
	 */
	@Deprecated
	public static <T extends Storable> DataViewStore<T> newInstance(final CMDataView view,
			final StorableConverter<T> converter) {
		return DataViewStore.<T> newInstance() //
				.withDataView(view) //
				.withGroupable(notGroupable()) //
				.withStorableConverter(converter) //
				.build();
	}

	/**
	 * @deprecated Use {@link newInstance()} instead.
	 */
	@Deprecated
	public static <T extends Storable> DataViewStore<T> newInstance(final CMDataView view, final Groupable groupable,
			final StorableConverter<T> converter) {
		return DataViewStore.<T> newInstance() //
				.withDataView(view) //
				.withGroupable(groupable) //
				.withStorableConverter(converter) //
				.build();
	}

	public static <T extends Storable> Builder<T> newInstance() {
		return new Builder<T>();
	}

	private final CMDataView dataView;
	private final Groupable groupable;
	private final StorableConverter<T> converter;
	private final Supplier<CMClass> storeClassHolder;

	private DataViewStore(final Builder<T> builder) {
		this.dataView = builder.dataView;
		this.groupable = builder.groupable;
		this.converter = builder.storableConverter;
		// TODO move within validation code
		this.storeClassHolder = memoize(new Supplier<CMClass>() {

			@Override
			public CMClass get() {
				final String className = converter.getClassName();
				final CMClass target = dataView.findClass(className);
				if (target == null) {
					logger.error(marker, "class '{}' has not been found", converter.getClassName());
					throw NotFoundException.NotFoundExceptionType.CLASS_NOTFOUND.createException(className);
				}
				return target;
			}

		});
	}

	private CMClass storeClass() {
		return storeClassHolder.get();
	}

	@Override
	public Storable create(final T storable) {
		logger.debug(marker, "creating a new storable element");

		logger.trace(marker, "filling new card's attributes");
		final CMCardDefinition card = converter.fill(dataView.createCardFor(storeClass()) //
				.setUser(converter.getUser(storable)), storable);

		logger.debug(marker, "saving card");
		return converter.storableOf(card.save());
	}

	@Override
	public T read(final Storable storable) {
		logger.info(marker, "reading storable element with identifier '{}'", storable.getIdentifier());

		final CMCard card = findCard(storable);

		logger.debug(marker, "converting card to storable element");
		return converter.convert(card);
	}

	@Override
	public Collection<T> readAll() {
		logger.debug(marker, "listing all storable elements");
		return readAll(notGroupable());
	}

	@Override
	public Collection<T> readAll(final Groupable groupable) {
		logger.debug(marker, "listing all storable elements with additional grouping condition '{}'", groupable);
		final CMQueryResult result = dataView //
				.select(anyAttribute(storeClass())) //
				.from(storeClass()) //
				.where(and(builtInGroupWhereClause(), groupWhereClause(groupable))) //
				.run();

		final List<T> list = transform(newArrayList(result), new Function<CMQueryRow, T>() {
			@Override
			public T apply(final CMQueryRow input) {
				return converter.convert(input.getCard(storeClass()));
			}
		});
		return list;
	}

	/**
	 * Creates a {@link WhereClause} for the grouping.
	 * 
	 * @return the {@link WhereClause} for the grouping, {@link TrueWhereClause}
	 *         if no grouping is available.
	 */
	private WhereClause builtInGroupWhereClause() {
		logger.debug(marker, "building built-in group where clause");
		return groupWhereClause(groupable);
	}

	private WhereClause groupWhereClause(final Groupable groupable) {
		logger.debug(marker, "building group where clause");
		final WhereClause clause;
		final String attributeName = groupable.getGroupAttributeName();
		if (attributeName != null) {
			logger.debug(marker, "group attribute name is '{}', building where clause", attributeName);
			final Object attributeValue = groupable.getGroupAttributeValue();
			clause = condition(attribute(storeClass(), attributeName), eq(attributeValue));
		} else {
			logger.debug(marker, "group attribute name not specified");
			clause = trueWhereClause();
		}
		return clause;
	}

	@Override
	public void update(final T storable) {
		logger.debug(marker, "updating storable element with identifier '{}'", storable.getIdentifier());

		logger.trace(marker, "filling existing card's attributes");
		final CMCard card = findCard(storable);
		final CMCardDefinition updatedCard = converter.fill(dataView.update(card) //
				.setUser(converter.getUser(storable)), storable);

		logger.debug(marker, "saving card");
		updatedCard.save();
	}

	@Override
	public void delete(final Storable storable) {
		logger.debug(marker, "deleting storable element with identifier '{}'", storable.getIdentifier());
		final CMCard cardToDelete = findCard(storable);
		dataView.delete(cardToDelete);
	}

	/**
	 * Returns the {@link CMCard} corresponding to the {@link Storable} object.<br>
	 */
	private CMCard findCard(final Storable storable) {
		logger.debug(marker, "looking for storable element with identifier '{}'", storable.getIdentifier());
		return dataView //
				.select(anyAttribute(storeClass())) //
				.from(storeClass()) //
				.where(whereClauseFor(storable)) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow() //
				.getCard(storeClass());
	}

	/**
	 * Builds the where clause for the specified {@link Storable} object.
	 */
	private WhereClause whereClauseFor(final Storable storable) {
		logger.debug(marker, "building specific where clause");

		final String attributeName = converter.getIdentifierAttributeName();
		final Object attributeValue;
		if (DEFAULT_IDENTIFIER_ATTRIBUTE_NAME.equals(attributeName)) {
			logger.debug(marker, "using default one identifier attribute, converting to default type");
			attributeValue = Long.parseLong(storable.getIdentifier());
		} else {
			attributeValue = storable.getIdentifier();
		}

		return and(builtInGroupWhereClause(), condition(attribute(storeClass(), attributeName), eq(attributeValue)));
	}

}
