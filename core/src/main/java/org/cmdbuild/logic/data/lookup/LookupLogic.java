package org.cmdbuild.logic.data.lookup;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.exception.ORMException.ORMExceptionType.ORM_CHANGE_LOOKUPTYPE_ERROR;
import static org.cmdbuild.logic.PrivilegeUtils.assure;
import static org.cmdbuild.logic.data.lookup.Util.actives;
import static org.cmdbuild.logic.data.lookup.Util.toLookupType;
import static org.cmdbuild.logic.data.lookup.Util.typesWith;
import static org.cmdbuild.logic.data.lookup.Util.uniques;
import static org.cmdbuild.logic.data.lookup.Util.withId;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ForwardingAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.view.CMAttributeDefinition;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.lookup.ForwardingLookup;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupImpl;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.logic.data.Utils.CMAttributeWrapper;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ordering;

public class LookupLogic implements Logic {

	private static final Marker marker = MarkerFactory.getMarker(LookupLogic.class.getName());

	public static interface LookupTypeQuery {

		Integer limit();

		Integer offset();

	}

	public static final LookupTypeQuery UNUSED_LOOKUP_TYPE_QUERY = new LookupTypeQuery() {

		@Override
		public Integer limit() {
			return null;
		}

		@Override
		public Integer offset() {
			return null;
		}

	};

	public static interface LookupQuery {

		Integer limit();

		Integer offset();

	}

	public static final LookupQuery UNUSED_LOOKUP_QUERY = new LookupQuery() {

		@Override
		public Integer limit() {
			return null;
		}

		@Override
		public Integer offset() {
			return null;
		}

	};

	private static class Exceptions {

		private Exceptions() {
			// prevents instantiation
		}

		public static NotFoundException lookupTypeNotFound(final LookupType type) {
			return NotFoundExceptionType.LOOKUP_TYPE_NOTFOUND.createException(type.name);
		}

		public static NotFoundException lookupNotFound(final Long id) {
			return NotFoundExceptionType.LOOKUP_NOTFOUND.createException(id.toString());
		}

		public static ORMException multipleElementsWithSameId() {
			return ORMExceptionType.ORM_UNIQUE_VIOLATION.createException();
		}

	}

	private static final Comparator<LookupType> NAME_ASC = new Comparator<LookupType>() {

		@Override
		public int compare(final LookupType o1, final LookupType o2) {
			final String v1 = o1.name;
			final String v2 = o2.name;
			return v1.compareTo(v2);
		}

	};

	private static final Comparator<Lookup> NUMBER_COMPARATOR = new Comparator<Lookup>() {
		@Override
		public int compare(final Lookup o1, final Lookup o2) {
			if (o1.number() > o2.number()) {
				return 1;
			} else if (o1.number() < o2.number()) {
				return -1;
			}
			return 0;
		}
	};

	private final LookupStore store;
	private final OperationUser operationUser;
	private final CMDataView dataView;

	public LookupLogic( //
			final LookupStore store, //
			final OperationUser operationUser, //
			final CMDataView dataView) {
		this.store = store;
		this.operationUser = operationUser;
		this.dataView = dataView;
	}

	public PagedElements<LookupType> getAllTypes(final LookupTypeQuery query) {
		logger.trace(marker, "getting all lookup types");
		final Iterable<LookupType> allElements = from(store.readAll()) //
				.transform(toLookupType()) //
				.filter(uniques());
		final Iterable<LookupType> ordered = Ordering.from(NAME_ASC).sortedCopy(allElements);
		final Integer offset = query.offset();
		final Integer limit = query.limit();
		final Iterable<LookupType> filtered = from(ordered) //
				.skip((offset == null) ? 0 : offset) //
				.limit((limit == null) ? Integer.MAX_VALUE : limit);
		return new PagedElements<LookupType>(filtered, size(ordered));
	}

	public String fetchTranslationUuid(final int id) {
		final LookupImpl lookupWithId = LookupImpl.newInstance().withId((long) id).build();
		try {
			final Lookup currentLookup = store.read(lookupWithId);
			return currentLookup.getTranslationUuid();
		} catch (final Throwable t) {
			return null;
		}
	}

	public void saveLookupType(final LookupType newType, final LookupType oldType) {
		logger.debug(marker, "saving lookup type, new is '{}', old is '{}'", newType, oldType);

		assure(operationUser.hasAdministratorPrivileges());

		if (isBlank(newType.name)) {
			logger.error("invalid name '{}' for lookup type", newType.name);
			throw ORM_CHANGE_LOOKUPTYPE_ERROR.createException();
		}

		final LookupType existingLookupType = typeForNameAndParent(oldType.name, oldType.parent);
		if (existingLookupType == null) {
			logger.debug(marker, "old one not specified, creating a new one");
			final Lookup lookup = LookupImpl.newInstance() //
					.withType(newType) //
					.withNumber(1) //
					.withActiveStatus(true) //
					.build();
			store.create(lookup);
		} else {
			logger.debug(marker, "old one specified, modifying existing one");
			for (final Lookup lookup : store.readAll(oldType)) {
				final Lookup newLookup = LookupImpl.newInstance() //
						.withId(lookup.getId()) //
						.withCode(lookup.code()) //
						.withDescription(lookup.description()) //
						.withType(newType) //
						.withNumber(lookup.number()) //
						.withActiveStatus(lookup.active()) //
						.withDefaultStatus(lookup.isDefault()) //
						.build();
				store.update(newLookup);
			}

			logger.info(marker, "updates existing classes' attributes");
			for (final CMClass existingClass : dataView.findClasses()) {
				logger.debug(marker, "examining class '{}'", existingClass.getIdentifier().getLocalName());
				for (final CMAttribute existingAttribute : existingClass.getAttributes()) {
					logger.debug(marker, "examining attribute '{}'", existingAttribute.getName());
					existingAttribute.getType().accept(new ForwardingAttributeTypeVisitor() {

						private final CMAttributeTypeVisitor DELEGATE = NullAttributeTypeVisitor.getInstance();

						@Override
						protected CMAttributeTypeVisitor delegate() {
							return DELEGATE;
						}

						@Override
						public void visit(final LookupAttributeType attributeType) {
							if (asList(oldType.name, newType.name).contains(attributeType.getLookupTypeName())) {
								dataView.updateAttribute(attribute(existingAttribute, newType));
							}
						}

						private CMAttributeDefinition attribute(final CMAttribute attribute, final LookupType type) {
							return new CMAttributeWrapper(attribute) {

								@Override
								public CMAttributeType<?> getType() {
									return new LookupAttributeType(type.name);
								}

							};
						}

					});
				}
			}
		}
	}

	public PagedElements<Lookup> getAllLookup( //
			final LookupType type, //
			final boolean activeOnly, //
			final LookupQuery query //
	) {
		logger.debug(marker, "getting all lookups for type '{}'", type);

		final Optional<LookupType> realType = typeFor(typesWith(type.name));
		if (!realType.isPresent()) {
			logger.error(marker, format("lookup type not found '%s'", type));
			throw Exceptions.lookupTypeNotFound(type);
		}

		logger.trace(marker, "getting all lookups for real type '{}'", realType);
		final Iterable<Lookup> elements = store.readAll(realType.get());

		if (!elements.iterator().hasNext()) {
			logger.error(marker, "no lookup was found for type '{}'", realType);
			throw Exceptions.lookupTypeNotFound(realType.get());
		}

		final List<Lookup> list = newArrayList(elements);

		logger.trace(marker, "ordering elements");
		sort(list, NUMBER_COMPARATOR);

		final Integer offset = query.offset();
		final Integer limit = query.limit();
		final FluentIterable<Lookup> all = from(list) //
				.filter(actives(activeOnly)) //
				.skip((offset == null) ? 0 : offset) //
				.limit((limit == null) ? Integer.MAX_VALUE : limit);
		return new PagedElements<Lookup>(all, size(list));
	}

	public Iterable<Lookup> getAllLookupOfParent(final LookupType type) {
		logger.debug(marker, "getting all lookups for the parent of type '{}'", type);
		final LookupType current = typeFor(typesWith(type.name)).orNull();
		if (current.parent == null) {
			return new LinkedList<Lookup>();
		}

		final LookupType parent = typeFor(typesWith(current.parent)).orNull();
		return store.readAll(parent);
	}

	public Lookup getLookup(final Long id) {
		logger.debug(marker, "getting lookup with id '{}'", id);
		final Iterator<Lookup> elements = from(store.readAll()) //
				.filter(new Predicate<Lookup>() {
					@Override
					public boolean apply(final Lookup input) {
						return input.getId().equals(id);
					};
				}) //
				.iterator();
		if (!elements.hasNext()) {
			throw Exceptions.lookupNotFound(id);
		}
		final Lookup lookup = elements.next();
		if (elements.hasNext()) {
			logger.error(marker, "multiple elements with id '{}'", id);
			throw Exceptions.multipleElementsWithSameId();
		}
		return lookup;
	}

	public void enableLookup(final Long id) {
		logger.debug(marker, "enabling lookup with id '{}'", id);
		assure(operationUser.hasAdministratorPrivileges());
		setActiveStatus(true, id);
	}

	public void disableLookup(final Long id) {
		logger.debug(marker, "disabling lookup with id '{}'", id);
		assure(operationUser.hasAdministratorPrivileges());
		setActiveStatus(false, id);
	}

	private void setActiveStatus(final boolean status, final Long id) {
		logger.debug(marker, "setting active status '{}' for lookup with id '{}'", status, id);
		if (id <= 0) {
			logger.warn(marker, "invalid id '{}', exiting without doing nothing", id);
			return;
		}

		logger.trace(marker, "getting lookup with id '{}'", id);
		final Optional<Lookup> element = from(store.readAll()) //
				.filter(withId(id)) //
				.first();

		if (!element.isPresent()) {
			throw Exceptions.lookupNotFound(id);
		}

		logger.trace(marker, "updating lookup active to '{}'", status);
		final Lookup lookup = new ForwardingLookup() {

			private final Lookup delegate = element.get();

			@Override
			protected Lookup delegate() {
				return delegate;
			}

			@Override
			public boolean active() {
				return status;
			}

		};

		store.update(lookup);
	}

	private LookupType typeForNameAndParent(final String name, final String parent) {
		logger.debug(marker, "getting lookup type with name '{}' and parent '{}'", name, parent);
		return typeFor(typesWith(name, parent)).orNull();
	}

	public LookupType typeFor(final String lookupTypeName) {
		return typeFor(typesWith(lookupTypeName)).orNull();
	}

	public Optional<LookupType> typeFor(final Predicate<LookupType> predicate) {
		logger.trace(marker, "getting lookup type for predicate");
		final Iterator<LookupType> shouldBeOneOnly = from(getAllTypes(UNUSED_LOOKUP_TYPE_QUERY)) //
				.filter(predicate) //
				.iterator();
		final Optional<LookupType> found;
		if (!shouldBeOneOnly.hasNext()) {
			found = Optional.absent();
		} else {
			found = Optional.of(shouldBeOneOnly.next());
		}
		if (found.isPresent() && shouldBeOneOnly.hasNext()) {
			logger.error(marker, "more than one lookup type has been found");
			throw ORMExceptionType.ORM_GENERIC_ERROR.createException();
		}
		return found;
	}

	public Long createOrUpdateLookup(final LookupImpl lookup) {
		logger.info(marker, "creating or updating lookup '{}'", lookup);

		assure(operationUser.hasAdministratorPrivileges());

		/*
		 * should be done outside the forwarding object due to unwanted
		 * recursion
		 */
		final LookupType realType = typeFor(typesWith(lookup.type().name)).orNull();
		final Lookup lookupWithRealType = new ForwardingLookup() {

			@Override
			protected Lookup delegate() {
				return lookup;
			}

			@Override
			public LookupType type() {
				return realType;
			}

		};

		final Long id;
		if (isNotExistent(lookupWithRealType)) {
			logger.info(marker, "creating lookup '{}'", lookupWithRealType);

			logger.debug(marker, "checking lookup number ('{}'), if not valid assigning a valid one",
					lookupWithRealType.number());
			final Lookup toBeCreated;
			if (hasNoValidNumber(lookupWithRealType)) {
				final int count = size(store.readAll(lookupWithRealType.type()));
				toBeCreated = new ForwardingLookup() {

					@Override
					protected Lookup delegate() {
						return lookupWithRealType;
					}

					@Override
					public Integer number() {
						return count + 1;
					}

				};
			} else {
				toBeCreated = lookupWithRealType;
			}

			final Storable created = store.create(toBeCreated);
			id = Long.valueOf(created.getIdentifier());
		} else {
			logger.info(marker, "updating lookup '{}'", lookupWithRealType);

			logger.debug(marker, "checking lookup number ('{}'), if not valid assigning a valid one",
					lookupWithRealType.number());
			final Lookup toBeUpdated;
			if (hasNoValidNumber(lookupWithRealType)) {
				final Lookup actual = store.read(lookupWithRealType);
				toBeUpdated = new ForwardingLookup() {

					@Override
					protected Lookup delegate() {
						return lookupWithRealType;
					}

					@Override
					public Integer number() {
						return actual.number();
					}

				};
			} else {
				toBeUpdated = lookupWithRealType;
			}

			store.update(toBeUpdated);
			id = lookupWithRealType.getId();
		}
		return id;
	}

	private static boolean isNotExistent(final Lookup lookup) {
		return lookup.getId() == null || lookup.getId() <= 0;
	}

	private static boolean hasNoValidNumber(final Lookup lookup) {
		return lookup.number() == null || lookup.number() <= 0;
	}

	/**
	 * Reorders lookups.
	 * 
	 * @param lookupType
	 *            the lookup's type of elements that must be ordered.
	 * @param positions
	 *            the positions of the elements; key is the id of the lookup
	 *            element, value is the new index.
	 */
	public void reorderLookup(final LookupType type, final Map<Long, Integer> positions) {
		logger.trace(marker, "reordering lookups for type '{}'", type);

		assure(operationUser.hasAdministratorPrivileges());

		final LookupType realType = typeFor(typesWith(type.name)).orNull();
		final Iterable<Lookup> lookups = store.readAll(realType);
		for (final Lookup lookup : lookups) {
			if (positions.containsKey(lookup.getId())) {
				final int index = positions.get(lookup.getId());
				final Lookup updated = new ForwardingLookup() {

					@Override
					protected Lookup delegate() {
						return lookup;
					}

					@Override
					public Integer number() {
						return index;
					}

				};
				store.update(updated);
			}
		}
	}

}
