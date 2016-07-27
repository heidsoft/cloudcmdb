package org.cmdbuild.logic.data;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_11;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_1N;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_N1;
import static org.cmdbuild.dao.entrytype.Predicates.attributeTypeInstanceOf;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.Clauses.call;
import static org.cmdbuild.logic.data.Utils.withClassOrder;
import static org.cmdbuild.logic.data.Utils.definitionForExisting;
import static org.cmdbuild.logic.data.Utils.definitionForNew;
import static org.cmdbuild.logic.data.Utils.withIndex;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMEntryTypeVisitor;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.ForwardingEntryTypeVisitor;
import org.cmdbuild.dao.entrytype.NullEntryTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ForwardingAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.clause.alias.NameAlias;
import org.cmdbuild.dao.view.CMAttributeDefinition;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.ForwardingAttributeDefinition;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.dao.DataViewStore;
import org.cmdbuild.data.store.metadata.ForwardingMetadata;
import org.cmdbuild.data.store.metadata.Metadata;
import org.cmdbuild.data.store.metadata.MetadataConverter;
import org.cmdbuild.data.store.metadata.MetadataGroupable;
import org.cmdbuild.data.store.metadata.MetadataImpl;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.ClassOrder;
import org.cmdbuild.model.data.Domain;
import org.cmdbuild.model.data.EntryType;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

/**
 * Business Logic Layer for data definition.
 */
public class DefaultDataDefinitionLogic implements DataDefinitionLogic {

	private static final Function<ClassOrder, String> ATTRIBUTE_NAME_AS_KEY = new Function<ClassOrder, String>() {

		@Override
		public String apply(final ClassOrder input) {
			return input.attributeName;
		}

	};

	private static class FunctionItemWrapper implements FunctionItem {

		private final CMFunction delegate;

		public FunctionItemWrapper(final CMFunction delegate) {
			this.delegate = delegate;
		}

		@Override
		public String name() {
			return delegate.getName();
		}

	}

	private static Function<CMFunction, FunctionItem> toFunctionItem = new Function<CMFunction, FunctionItem>() {

		@Override
		public FunctionItem apply(final CMFunction input) {
			return new FunctionItemWrapper(input);
		}

	};

	private static final String UPDATE_CLASS_INDEXES_FUNCTION_NAME = "_cm_create_class_default_order_indexes";

	private static final NameAlias FUNCTION_ALIAS = NameAlias.as("f");

	private static final Iterable<String> NO_DISABLED = emptyList();

	private static CMClass NO_PARENT = null;

	private final CMDataView view;

	public DefaultDataDefinitionLogic(final CMDataView dataView) {
		this.view = dataView;
	}

	@Override
	public CMDataView getView() {
		return view;
	}

	/**
	 * if forceCreation is true, check if already exists a table with the same
	 * name of the given entryType
	 */
	@Override
	public CMClass createOrUpdate(final EntryType entryType, final boolean forceCreation) {
		if (forceCreation && view.findClass(entryType.getName()) != null) {

			throw ORMExceptionType.ORM_DUPLICATE_TABLE.createException();
		}

		return createOrUpdate(entryType);
	}

	@Override
	public CMClass createOrUpdate(final EntryType entryType) {
		logger.info("creating or updating class '{}'", entryType);

		final CMClass existingClass = view.findClass(identifierFrom(entryType));

		final Long parentId = entryType.getParentId();
		final CMClass parentClass = (parentId == null) ? NO_PARENT : view.findClass(parentId.longValue());

		final CMClass createdOrUpdatedClass;
		if (existingClass == null) {
			logger.info("class not already created, creating a new one");
			createdOrUpdatedClass = view.create(definitionForNew(entryType, parentClass));
		} else {
			logger.info("class already created, updating existing one");
			createdOrUpdatedClass = view.update(definitionForExisting(entryType, existingClass));
		}
		return createdOrUpdatedClass;
	}

	private CMIdentifier identifierFrom(final EntryType entryType) {
		return identifierFrom(entryType.getName(), entryType.getNamespace());
	}

	private CMIdentifier identifierFrom(final String localname, final String namespace) {
		return new CMIdentifier() {

			@Override
			public String getLocalName() {
				return localname;
			}

			@Override
			public String getNameSpace() {
				return namespace;
			}

		};
	}

	/**
	 * TODO: delete also privileges that refers to the deleted class
	 */
	@Override
	public void deleteOrDeactivate(final String className) {
		logger.info("deleting class '{}'", className);
		final CMClass existingClass = view.findClass(className);
		if (existingClass == null) {
			logger.warn("class '{}' not found", className);
			return;
		}
		final boolean hasChildren = !isEmpty(existingClass.getChildren());
		if (existingClass.isSuperclass() && hasChildren) {
			throw ORMException.ORMExceptionType.ORM_TABLE_HAS_CHILDREN.createException();
		}
		try {
			logger.warn("deleting existing class '{}'", className);
			view.delete(existingClass);
		} catch (final Exception e) {
			logger.error("error deleting class", e);
			logger.warn("class contains data");
			throw ORMException.ORMExceptionType.ORM_CONTAINS_DATA.createException();
		}

	}

	@Override
	public CMAttribute createOrUpdate(final Attribute attribute) {
		logger.info("creating or updating attribute '{}'", attribute.toString());

		final CMEntryType owner = findOwnerOf(attribute);
		final CMAttribute existingAttribute = owner.getAttribute(attribute.getName());

		logger.info("checking common pre-conditions");
		if (existingAttribute == null) {
			logger.info("force for the new attribute to have the last (1 based) index");
			final int numberOfAttribute = size(owner.getAttributes());
			attribute.setIndex(numberOfAttribute + 1);
		}

		logger.info("checking specific pre-conditions");
		owner.accept(new ForwardingEntryTypeVisitor() {

			private final CMEntryTypeVisitor delegate = NullEntryTypeVisitor.getInstance();

			@Override
			protected CMEntryTypeVisitor delegate() {
				return delegate;
			}

			@Override
			public void visit(final CMClass type) {
				attribute.getType().accept(new ForwardingAttributeTypeVisitor() {

					private final CMAttributeTypeVisitor delegate = NullAttributeTypeVisitor.getInstance();

					@Override
					protected CMAttributeTypeVisitor delegate() {
						return delegate;
					}

					@Override
					public void visit(final ReferenceAttributeType attributeType) {
						logger.info("checking domain");
						final String domainName = attributeType.getDomainName();
						final CMDomain domain = view.findDomain(domainName);
						if (domain == null) {
							throw NotFoundExceptionType.DOMAIN_NOTFOUND.createException(domainName);
						}

						logger.info("checking namespace");
						final CMIdentifier identifier = attributeType.getIdentifier();
						Validate.isTrue(identifier.getNameSpace() == CMIdentifier.DEFAULT_NAMESPACE,
								"non-default namespaces not supported at this level");

						logger.info("checking cardinality");
						Validate.isTrue(asList(CARDINALITY_1N.value(), CARDINALITY_N1.value()).contains(
								domain.getCardinality()));
					}

				});
			}

		});

		final CMAttributeDefinition definition;
		final CMAttribute createdOrUpdatedAttribute;
		if (existingAttribute == null) {
			logger.info("attribute not already created, creating a new one");
			definition = definitionForNew(attribute, owner);
			createdOrUpdatedAttribute = view.createAttribute(definition);
		} else {
			logger.info("attribute already created, updating existing one");
			definition = definitionForExisting(existingAttribute, attribute);
			createdOrUpdatedAttribute = view.updateAttribute(definition);
		}

		logger.info("checking post-conditions");
		owner.accept(new ForwardingEntryTypeVisitor() {

			private final CMEntryTypeVisitor delegate = NullEntryTypeVisitor.getInstance();

			@Override
			protected CMEntryTypeVisitor delegate() {
				return delegate;
			}

			@Override
			public void visit(final CMClass type) {
				attribute.getType().accept(new ForwardingAttributeTypeVisitor() {

					private final CMAttributeTypeVisitor delegate = NullAttributeTypeVisitor.getInstance();

					@Override
					protected CMAttributeTypeVisitor delegate() {
						return delegate;
					}

					@Override
					public void visit(final ReferenceAttributeType attributeType) {
						if (attribute.isActive()) {
							logger.info("checking disabled classes for domain");
							final String domainName = attributeType.getDomainName();
							final CMDomain domain = view.findDomain(domainName);
							final Iterable<String> disabled;
							final String cardinality = domain.getCardinality();
							if (CARDINALITY_1N.value().equals(cardinality)) {
								disabled = domain.getDisabled2();
							} else if (CARDINALITY_N1.value().equals(cardinality)) {
								disabled = domain.getDisabled1();
							} else {
								throw new AssertionError("should never come here");
							}
							if (!isEmpty(disabled)) {
								fixAttribute(type, disabled);
							}
						}
					}

					private void fixAttribute(final CMClass target, final Iterable<String> disabled) {
						logger.info("updating attribute for (sub)class '{}'", target.getName());
						view.updateAttribute(new ForwardingAttributeDefinition() {

							@Override
							protected CMAttributeDefinition delegate() {
								return definition;
							}

							@Override
							public CMEntryType getOwner() {
								return target;
							}

							@Override
							public Boolean isActive() {
								return !from(disabled) //
										.contains(target.getName());
							}

						});
						for (final CMClass subclass : target.getChildren()) {
							fixAttribute(subclass, disabled);
						}
					}

				});
			}

		});

		logger.info("setting metadata for attribute '{}'", attribute.getName());
		final Store<Metadata> store = DataViewStore.<Metadata> newInstance() //
				.withDataView(view) //
				.withGroupable(MetadataGroupable.of(createdOrUpdatedAttribute)) //
				.withStorableConverter(MetadataConverter.of(MetadataGroupable.of(createdOrUpdatedAttribute))) //
				.build();
		final Map<String, String> received = newHashMap(attribute.getMetadata());
		// TODO check/validate received values?
		for (final Metadata existing : store.readAll()) {
			if (received.containsKey(existing.name())) {
				final String newValue = received.get(existing.name());
				if (!ObjectUtils.equals(existing.value(), newValue)) {
					store.update(new ForwardingMetadata() {

						@Override
						protected Metadata delegate() {
							return existing;
						}

						@Override
						public String value() {
							return newValue;
						}

					});
				}
			} else {
				store.delete(existing);
			}
			received.remove(existing.name());
		}
		for (final Entry<String, String> entry : received.entrySet()) {
			store.create(MetadataImpl.of(entry.getKey(), entry.getValue()));
		}

		return createdOrUpdatedAttribute;
	}

	private CMEntryType findOwnerOf(final Attribute attribute) {
		logger.debug("getting entry type with name '{}' and namespace '{}'", attribute.getOwnerName(),
				attribute.getOwnerNamespace());
		CMEntryType entryType;

		final CMIdentifier identifier = identifierFrom(attribute.getOwnerName(), attribute.getOwnerNamespace());

		// try with classes
		entryType = view.findClass(identifier);
		if (entryType != null) {
			logger.debug("class found");
			return entryType;
		}

		// try with domains
		entryType = view.findDomain(identifier);
		if (entryType != null) {
			logger.debug("domain found");
			return entryType;
		}

		logger.warn("not found");
		throw ORMExceptionType.ORM_TYPE_ERROR.createException();
	}

	@Override
	public void deleteOrDeactivate(final Attribute attribute) {
		logger.info("deleting attribute '{}'", attribute.toString());
		final CMEntryType owner = findOwnerOf(attribute);
		final CMAttribute existingAttribute = owner.getAttribute(attribute.getName());
		if (existingAttribute == null) {
			logger.warn("attribute '{}' not found", attribute.getName());
			return;
		}
		try {
			logger.info("deleting metadata for attribute '{}'", attribute.getName());
			final Store<Metadata> store = DataViewStore.<Metadata> newInstance() //
					.withDataView(view) //
					.withGroupable(MetadataGroupable.of(existingAttribute)) //
					.withStorableConverter(MetadataConverter.of(MetadataGroupable.of(existingAttribute))) //
					.build();
			for (final Metadata metadata : store.readAll()) {
				store.delete(metadata);
			}

			logger.info("deleting existing attribute '{}'", attribute.getName());
			view.delete(existingAttribute);
		} catch (final Exception e) {
			logger.warn("error deleting attribute", e);
			/**
			 * TODO: move the throw exception to dao level when all exception
			 * system will be re-organized. Here catch only an ORM_CONTAINS_DATA
			 * exception, thrown from dao
			 */
			if (e.getMessage().contains("CM_CONTAINS_DATA")) {
				throw ORMExceptionType.ORM_CONTAINS_DATA.createException();
			}
		}
	}

	@Override
	public void reorder(final Attribute attribute) {
		logger.info("reordering attribute '{}'", attribute.toString());
		final CMEntryType owner = findOwnerOf(attribute);
		final CMAttribute existingAttribute = owner.getAttribute(attribute.getName());
		if (existingAttribute == null) {
			logger.warn("attribute '{}' not found", attribute.getName());
		} else if (existingAttribute.getIndex() == attribute.getIndex()) {
			logger.debug("index for attribute '{}' not changed", attribute.getName());
		} else {
			view.updateAttribute(withIndex(existingAttribute, attribute.getIndex()));
		}
	}

	@Override
	public void changeClassOrders(final String className, final List<ClassOrder> classOrders) {
		logger.info("changing classorders '{}' for class '{}'", classOrders, className);

		final Map<String, ClassOrder> mappedClassOrders = Maps.uniqueIndex(classOrders, ATTRIBUTE_NAME_AS_KEY);

		final CMClass owner = view.findClass(className);
		for (final CMAttribute attribute : owner.getAttributes()) {
			view.updateAttribute(withClassOrder(attribute, //
					valueOrDefaultIfNull(mappedClassOrders.get(attribute.getName()))));
		}

		final CMFunction function = view.findFunctionByName(UPDATE_CLASS_INDEXES_FUNCTION_NAME);
		if (function != null) {
			final Object[] actualParams = new Object[] { className };
			view.select(anyAttribute(function, FUNCTION_ALIAS)) //
					.from(call(function, actualParams), FUNCTION_ALIAS) //
					.run();
		}
	}

	private int valueOrDefaultIfNull(final ClassOrder classOrder) {
		return (classOrder == null) ? 0 : classOrder.value;
	}

	@Override
	public CMDomain create(final Domain domain) {
		final CMDomain existing = view.findDomain(domain.getName());
		final CMDomain createdDomain;
		if (existing != null) {
			logger.error("Error creating a domain with name {}. A domain with the same name already exists.",
					domain.getName());
			throw ORMExceptionType.ORM_ERROR_DOMAIN_CREATE.createException();
		}
		logger.info("Domain not already created, creating a new one");
		final CMClass class1 = view.findClass(domain.getIdClass1());
		final CMClass class2 = view.findClass(domain.getIdClass2());
		createdDomain = view.create(definitionForNew(domain, class1, class2));
		return createdDomain;
	}

	@Override
	public CMDomain update(final Domain domain) {
		final CMDomain existing = view.findDomain(domain.getName());
		final CMDomain updatedDomain;
		if (existing == null) {
			logger.error("Cannot update the domain with name {}. It does not exist", domain.getName());
			throw NotFoundExceptionType.DOMAIN_NOTFOUND.createException(domain.getName());
		}

		validateActivationForReferences(domain);

		logger.info("Updating domain with name {}", domain.getName());
		updatedDomain = view.update(definitionForExisting(domain, existing));
		return updatedDomain;
	}

	private void validateActivationForReferences(final Domain domain) {
		final Iterable<String> allDisabled = "N:1".equals(domain.getCardinality()) ? defaultIfNull(
				domain.getDisabled1(), NO_DISABLED) : defaultIfNull(domain.getDisabled2(), NO_DISABLED);
		for (final String disabled : allDisabled) {
			final CMClass target = view.findClass(disabled);
			if (target == null) {
				throw NotFoundExceptionType.CLASS_NOTFOUND.createException(disabled);
			}
			for (final CMAttribute attribute : from(target.getActiveAttributes()) //
					.filter(attributeTypeInstanceOf(ReferenceAttributeType.class))) {
				attribute.getType().accept(new ForwardingAttributeTypeVisitor() {

					private final CMAttributeTypeVisitor DELEGATE = NullAttributeTypeVisitor.getInstance();

					@Override
					protected CMAttributeTypeVisitor delegate() {
						return DELEGATE;
					}

					@Override
					public void visit(final ReferenceAttributeType attributeType) {
						if (attributeType.getDomainName().equals(domain.getName()) && attribute.isActive()) {
							throw ORMExceptionType.ORM_ACTIVE_ATTRIBUTE.createException(target.getName(),
									attribute.getName());
						}
					}

				});
			}
		}
	}

	@Override
	public void deleteDomainIfExists(final String name) {
		logger.info("deleting domain '{}'", name);

		final CMDomain domain = view.findDomain(name);
		if (domain == null) {
			logger.warn("domain '{}' not found", name);
		} else {
			final boolean hasReference;
			final String cardinality = domain.getCardinality();
			if (asList(CARDINALITY_11.value(), CARDINALITY_1N.value()).contains(cardinality)) {
				final CMClass table = view.findClass(domain.getClass2().getName());
				hasReference = searchReference(table, domain);
			} else if (asList(CARDINALITY_11.value(), CARDINALITY_N1.value()).contains(cardinality)) {
				final CMClass table = view.findClass(domain.getClass1().getName());
				hasReference = searchReference(table, domain);
			} else {
				hasReference = false;
			}

			if (hasReference) {
				throw ORMExceptionType.ORM_DOMAIN_HAS_REFERENCE.createException();
			} else {
				view.delete(domain);
			}
		}
	}

	private static boolean searchReference(final CMClass table, final CMDomain domain) {
		if (classContainsReferenceAttributeToDomain(table, domain)) {
			return true;
		}
		for (final CMClass descendant : table.getDescendants()) {
			if (classContainsReferenceAttributeToDomain(descendant, domain)) {
				return true;
			}
		}
		return false;
	}

	private static boolean classContainsReferenceAttributeToDomain(final CMClass table, final CMDomain domain) {
		for (final CMAttribute attribute : table.getAttributes()) {
			final CMAttributeType<?> attributeType = attribute.getType();
			if (attributeType instanceof ReferenceAttributeType) {
				final ReferenceAttributeType referenceAttributeType = ReferenceAttributeType.class.cast(attributeType);
				final String referenceDomainName = referenceAttributeType.getIdentifier().getLocalName();
				if (referenceDomainName.equals(domain.getIdentifier().getLocalName())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Iterable<FunctionItem> functions() {
		return from(view.findAllFunctions()) //
				.transform(toFunctionItem);
	}

}
