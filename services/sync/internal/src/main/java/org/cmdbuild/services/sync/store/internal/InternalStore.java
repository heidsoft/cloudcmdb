package org.cmdbuild.services.sync.store.internal;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.addAll;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.dao.driver.postgres.Const.ID_ATTRIBUTE;
import static org.cmdbuild.dao.guava.Functions.toCard;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.services.sync.store.CardEntry;
import org.cmdbuild.services.sync.store.ClassType;
import org.cmdbuild.services.sync.store.Entry;
import org.cmdbuild.services.sync.store.Key;
import org.cmdbuild.services.sync.store.Store;
import org.cmdbuild.services.sync.store.Type;
import org.cmdbuild.services.sync.store.TypeVisitor;

import com.google.common.base.Predicate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;

public class InternalStore implements Store {

	private static final AttributeValueAdapter NULL_ATTRIBUTE_VALUE_ADAPTER = new AttributeValueAdapter() {

		@Override
		public Iterable<Map.Entry<String, Object>> toSynchronizer(final ClassType type,
				final Iterable<? extends Map.Entry<String, ? extends Object>> values) {
			return identity(values);
		}

		@Override
		public Iterable<Map.Entry<String, Object>> toInternal(final ClassType type,
				final Iterable<? extends Map.Entry<String, ? extends Object>> values) {
			return identity(values);
		}

		private Iterable<Map.Entry<String, Object>> identity(
				final Iterable<? extends Map.Entry<String, ? extends Object>> values) {
			final Map<String, Object> adapted = Maps.newHashMap();
			for (final Map.Entry<String, ? extends Object> entry : values) {
				adapted.put(entry.getKey(), entry.getValue());
			}
			return adapted.entrySet();
		}

	};

	public static class Builder implements org.apache.commons.lang3.builder.Builder<InternalStore> {

		private CMDataView dataView;
		private Catalog catalog;
		private AttributeValueAdapter attributeValueAdapter;

		private Builder() {
			// use factory method
		}

		@Override
		public InternalStore build() {
			validate();
			return new InternalStore(this);
		}

		private void validate() {
			Validate.notNull(dataView, "missing '%s'", CMDataView.class);
			Validate.notNull(catalog, "missing '%s'", Catalog.class);
			attributeValueAdapter = defaultIfNull(attributeValueAdapter, NULL_ATTRIBUTE_VALUE_ADAPTER);
		}

		public Builder withDataView(final CMDataView dataView) {
			this.dataView = dataView;
			return this;
		}

		public Builder withCatalog(final Catalog catalog) {
			this.catalog = catalog;
			return this;
		}

		public Builder setAttributeValueAdapter(final AttributeValueAdapter attributeValueAdapter) {
			this.attributeValueAdapter = attributeValueAdapter;
			return this;
		}

		public Builder withAttributeValueAdapter(final AttributeValueAdapter attributeValueAdapter) {
			this.attributeValueAdapter = attributeValueAdapter;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private static class AttributesOfType implements Predicate<Map.Entry<String, ? extends Object>> {

		public static AttributesOfType of(final Type type) {
			return new AttributesOfType(type);
		}

		private final Type type;

		private AttributesOfType(final Type type) {
			this.type = type;
		}

		@Override
		public boolean apply(final Map.Entry<String, ? extends Object> input) {
			return (type.getAttribute(input.getKey()) != null);
		}

	}

	private static abstract class Action<T> {

		protected final CMDataView dataView;

		protected Action(final CMDataView dataView) {
			this.dataView = dataView;
		}

		public abstract T execute();

	}

	private static class Create extends Action<Void> implements TypeVisitor {

		private final Entry entry;
		private final AttributeValueAdapter attributeValueAdapter;

		public Create(final CMDataView dataView, final AttributeValueAdapter attributeValueAdapter, final Entry entry) {
			super(dataView);
			this.attributeValueAdapter = attributeValueAdapter;
			this.entry = entry;
		}

		@Override
		public Void execute() {
			entry.getType().accept(this);
			return null;
		}

		@Override
		public void visit(final ClassType type) {
			final String typeName = type.getName();
			final CMClass targetType = dataView.findClass(typeName);
			// TODO handle class not found
			final Iterable<Map.Entry<String, Object>> rawValues = entry.getValues();
			final Iterable<Map.Entry<String, Object>> adaptedValues = attributeValueAdapter.toInternal(type, rawValues);
			dataView.createCardFor(targetType) //
					.set(adaptedValues) //
					.save();
			// TODO id and key
		}

	}

	private static class ReadAllByType extends Action<Iterable<Entry>> implements TypeVisitor {

		private final Cache<Key, Long> cache;
		private final AttributeValueAdapter attributeValueAdapter;
		private final Type type;
		private final Collection<Entry> entries;

		public ReadAllByType(final CMDataView dataView, final Cache<Key, Long> cache,
				final AttributeValueAdapter attributeValueAdapter, final Type type) {
			super(dataView);
			this.cache = cache;
			this.attributeValueAdapter = attributeValueAdapter;
			this.type = type;
			this.entries = newHashSet();
		}

		@Override
		public Iterable<Entry> execute() {
			type.accept(this);
			return entries;
		}

		@Override
		public void visit(final ClassType type) {
			final String typeName = type.getName();
			final CMClass targetType = dataView.findClass(typeName);
			// TODO handle class not found
			final Iterable<CMCard> cards = from(dataView.select(anyAttribute(targetType)) //
					.from(targetType) //
					.run()) //
					.transform(toCard(targetType));
			for (final CMCard card : cards) {
				final Iterable<Map.Entry<String, Object>> rawValues = from(card.getAllValues()) //
						.filter(AttributesOfType.of(type));
				final Iterable<Map.Entry<String, Object>> adaptedValues = attributeValueAdapter.toSynchronizer(type,
						rawValues);
				final CardEntry entry = CardEntry.newInstance() //
						.withType(type) //
						.withValues(adaptedValues) //
						.build();
				cache.put(entry.getKey(), card.getId());
				entries.add(entry);
			}
		}

	}

	private static class Update extends Action<Void> implements TypeVisitor {

		private final Cache<Key, Long> cache;
		private final AttributeValueAdapter attributeValueAdapter;
		private final Entry entry;

		public Update(final CMDataView dataView, final Cache<Key, Long> cache,
				final AttributeValueAdapter attributeValueAdapter, final Entry entry) {
			super(dataView);
			this.cache = cache;
			this.attributeValueAdapter = attributeValueAdapter;
			this.entry = entry;
		}

		@Override
		public Void execute() {
			entry.getType().accept(this);
			return null;
		}

		@Override
		public void visit(final ClassType type) {
			final String typeName = type.getName();
			final CMClass targetType = dataView.findClass(typeName);
			// TODO handle class not found
			final Long id = cache.getIfPresent(entry.getKey());
			// TODO handle id not found
			final CMCard card = dataView //
					.select(anyAttribute(targetType)) //
					.from(targetType) //
					.where(condition(attribute(targetType, ID_ATTRIBUTE), eq(id))) //
					.limit(1) //
					.skipDefaultOrdering() //
					.run() //
					.getOnlyRow() //
					.getCard(targetType);
			// TODO handle card not found
			final Iterable<Map.Entry<String, Object>> rawValues = entry.getValues();
			final Iterable<Map.Entry<String, Object>> adaptedValues = attributeValueAdapter.toInternal(type, rawValues);
			dataView.update(card) //
					.set(adaptedValues) //
					.save();
		}

	}

	private static class Delete extends Action<Void> implements TypeVisitor {

		private final Cache<Key, Long> cache;
		private final Entry entry;

		public Delete(final CMDataView dataView, final Cache<Key, Long> cache, final Entry entry) {
			super(dataView);
			this.cache = cache;
			this.entry = entry;
		}

		@Override
		public Void execute() {
			entry.getType().accept(this);
			return null;
		}

		@Override
		public void visit(final ClassType type) {
			final String typeName = type.getName();
			final CMClass targetType = dataView.findClass(typeName);
			// TODO handle class not found
			final Long id = cache.getIfPresent(entry.getKey());
			// TODO handle id not found
			final CMCard card = dataView //
					.select(anyAttribute(targetType)) //
					.from(targetType) //
					.where(condition(attribute(targetType, ID_ATTRIBUTE), eq(id))) //
					.limit(1) //
					.skipDefaultOrdering() //
					.run() //
					.getOnlyRow() //
					.getCard(targetType);
			// TODO handle card not found
			dataView.delete(card);
		}

	}

	private final CMDataView dataView;
	private final Catalog catalog;
	private final AttributeValueAdapter attributeValueAdapter;
	private final Cache<Key, Long> cache;

	private InternalStore(final Builder builder) {
		this.dataView = builder.dataView;
		this.catalog = builder.catalog;
		this.attributeValueAdapter = builder.attributeValueAdapter;
		this.cache = CacheBuilder.newBuilder() //
				.build();
	}

	@Override
	public void create(final Entry entry) {
		execute(doCreate(entry));
	}

	@Override
	public Iterable<Entry> readAll() {
		final Collection<Entry> allEntries = newHashSet();
		for (final Type type : catalog.getTypes()) {
			final Iterable<Entry> entriesOfType = execute(doReadAll(type));
			addAll(allEntries, entriesOfType);
		}
		return allEntries;
	}

	@Override
	public void update(final Entry entry) {
		execute(doUpdate(entry));
	}

	@Override
	public void delete(final Entry entry) {
		execute(doDelete(entry));
	}

	private Create doCreate(final Entry entry) {
		return new Create(dataView, attributeValueAdapter, entry);
	}

	private ReadAllByType doReadAll(final Type type) {
		return new ReadAllByType(dataView, cache, attributeValueAdapter, type);
	}

	private Update doUpdate(final Entry entry) {
		return new Update(dataView, cache, attributeValueAdapter, entry);
	}

	private Delete doDelete(final Entry entry) {
		return new Delete(dataView, cache, entry);
	}

	private <T> T execute(final Action<T> action) {
		return action.execute();
	}

}
