package org.cmdbuild.logic.taskmanager.task.connector;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_1N;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_N1;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.data.store.lookup.Predicates.lookupTypeWithName;
import static org.cmdbuild.data.store.lookup.Predicates.lookupWithDescription;

import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entry.LookupValue;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ForwardingAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.services.sync.store.ClassType;
import org.cmdbuild.services.sync.store.internal.AttributeValueAdapter;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

public class DefaultAttributeValueAdapter implements AttributeValueAdapter {

	private final CMDataView dataView;
	private final LookupStore lookupStore;

	public DefaultAttributeValueAdapter(final CMDataView dataView, final LookupStore lookupStore) {
		this.dataView = dataView;
		this.lookupStore = lookupStore;
	}

	@Override
	public Iterable<Map.Entry<String, Object>> toInternal(final ClassType type,
			final Iterable<? extends Map.Entry<String, ? extends Object>> values) {
		final String typeName = type.getName();
		final CMClass targetType = dataView.findClass(typeName);
		// TODO handle class not found
		final Map<String, Object> adapted = Maps.newHashMap();
		for (final Map.Entry<String, ? extends Object> entry : values) {
			final String attributeName = entry.getKey();
			final Object attributeValue = entry.getValue();
			new ForwardingAttributeTypeVisitor() {

				private final CMAttributeTypeVisitor DELEGATE = NullAttributeTypeVisitor.getInstance();

				private Object adaptedValue;

				@Override
				protected CMAttributeTypeVisitor delegate() {
					return DELEGATE;
				}

				public void adapt() {
					adaptedValue = attributeValue;
					if (attributeValue != null) {
						targetType.getAttribute(attributeName).getType().accept(this);
					}
					adapted.put(attributeName, adaptedValue);
				}

				@Override
				public void visit(final ReferenceAttributeType attributeType) {
					if (attributeValue instanceof String) {
						final String shouldBeCode = attributeValue.toString();
						final String domainName = attributeType.getDomainName();
						final CMDomain domain = dataView.findDomain(domainName);
						if (domain != null) {
							// retrieve the destination
							final String cardinality = domain.getCardinality();
							CMClass destination = null;
							if (CARDINALITY_1N.value().equals(cardinality)) {
								destination = domain.getClass1();
							} else if (CARDINALITY_N1.value().equals(cardinality)) {
								destination = domain.getClass2();
							}
							if (destination != null) {
								final CMQueryResult queryResult = dataView.select(anyAttribute(destination)) //
										.from(destination)
										//
										.where(condition(attribute(destination, DESCRIPTION_ATTRIBUTE),
												eq(shouldBeCode))) //
										.run();
								if (!queryResult.isEmpty()) {
									final CMQueryRow row = queryResult.iterator().next();
									final CMCard referredCard = row.getCard(destination);
									adaptedValue = referredCard.getId();
								} else {
									throw new RuntimeException("Conversion error");
								}
							} else {
								throw new RuntimeException("Conversion error");
							}
						}
					} else {
						adaptedValue = attributeValue;
					}
				}

				@Override
				public void visit(final LookupAttributeType attributeType) {
					final String lookupTypeName = attributeType.getLookupTypeName();
					final LookupType lookupType = from(lookupStore.readAllTypes()) //
							.filter(lookupTypeWithName(lookupTypeName)) //
							.first() //
							.get();
					final String shouldBeDescription = attributeValue.toString();
					final Optional<Lookup> lookup = from(lookupStore.readAll(lookupType)) //
							.filter(lookupWithDescription(shouldBeDescription)) //
							.first();
					adaptedValue = lookup.isPresent() ? lookup.get().getId() : null;
				}

			}.adapt();
		}
		return adapted.entrySet();
	}

	@Override
	public Iterable<Map.Entry<String, Object>> toSynchronizer(final ClassType type,
			final Iterable<? extends Map.Entry<String, ? extends Object>> values) {
		final String typeName = type.getName();
		final CMClass targetType = dataView.findClass(typeName);
		// TODO handle class not found
		final Map<String, Object> adapted = Maps.newHashMap();
		for (final Map.Entry<String, ? extends Object> entry : values) {
			final String attributeName = entry.getKey();
			final Object attributeValue = entry.getValue();
			new ForwardingAttributeTypeVisitor() {

				private final CMAttributeTypeVisitor DELEGATE = NullAttributeTypeVisitor.getInstance();

				private Object adaptedValue;

				@Override
				protected CMAttributeTypeVisitor delegate() {
					return DELEGATE;
				}

				public void adapt() {
					adaptedValue = attributeValue;
					targetType.getAttribute(attributeName).getType().accept(this);
					adapted.put(attributeName, adaptedValue);
				}

				@Override
				public void visit(final LookupAttributeType attributeType) {
					final LookupValue lookupValue = LookupValue.class.cast(attributeValue);
					adaptedValue = lookupValue.getDescription();
				}

				@Override
				public void visit(final ReferenceAttributeType attributeType) {
					final IdAndDescription referenceValue = IdAndDescription.class.cast(attributeValue);
					adaptedValue = referenceValue.getDescription();
				}

			}.adapt();
		}
		return adapted.entrySet();
	}

}