package org.cmdbuild.logic.filter;

import static com.google.common.base.Functions.identity;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.difference;
import static com.google.common.collect.Maps.uniqueIndex;
import static java.lang.Integer.MAX_VALUE;
import static java.util.stream.StreamSupport.stream;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.services.store.filter.FilterDTO;
import org.cmdbuild.services.store.filter.FilterStore;
import org.cmdbuild.services.store.filter.ForwardingFilter;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.MapDifference;

public class DefaultFilterLogic implements FilterLogic {

	public static interface Converter {

		FilterStore.Filter logicToStore(Filter filter);

		Filter storeToLogic(FilterStore.Filter filter);

	}

	public static class DefaultConverter implements Converter {

		private final com.google.common.base.Converter<Filter, FilterStore.Filter> delegate;

		public DefaultConverter(final com.google.common.base.Converter<Filter, FilterStore.Filter> delegate) {
			this.delegate = delegate;
		}

		@Override
		public FilterStore.Filter logicToStore(final Filter filter) {
			return delegate.convert(filter);
		}

		@Override
		public Filter storeToLogic(final FilterStore.Filter filter) {
			return delegate.reverse().convert(filter);
		}

	}

	public static class FilterConverter extends com.google.common.base.Converter<Filter, FilterStore.Filter> {

		private final UserStore userStore;

		public FilterConverter(final UserStore userStore) {
			this.userStore = userStore;
		}

		@Override
		protected FilterStore.Filter doForward(final Filter a) {
			return FilterDTO.newFilter() //
					.withId(a.getId()) //
					.withName(a.getName()) //
					.withDescription(a.getDescription()) //
					.withConfiguration(a.getConfiguration()) //
					.withClassName(a.getClassName()) //
					.thatIsShared(a.isShared()) //
					.withUserId(userStore.getUser().getAuthenticatedUser().getId()) //
					.build();
		}

		@Override
		protected Filter doBackward(final FilterStore.Filter b) {
			return new Filter() {

				@Override
				public Long getId() {
					return b.getId();
				}

				@Override
				public String getName() {
					return b.getName();
				}

				@Override
				public String getDescription() {
					return b.getDescription();
				}

				@Override
				public String getClassName() {
					return b.getClassName();
				}

				@Override
				public String getConfiguration() {
					return b.getConfiguration();
				}

				@Override
				public boolean isShared() {
					return b.isShared();
				}

			};
		}
	}

	private static final Marker MARKER = MarkerFactory.getMarker(FilterLogic.class.getName());

	private final FilterStore store;
	private final Converter converter;
	private final UserStore userStore;

	public DefaultFilterLogic(final FilterStore store, final Converter converter, final UserStore userStore) {
		this.store = store;
		this.converter = converter;
		this.userStore = userStore;
	}

	@Override
	public Filter create(final Filter filter) {
		logger.info(MARKER, "creating filter '{}'", filter);
		Validate.notBlank(filter.getName(), "missing name");
		final FilterStore.Filter _filter = converter.logicToStore(filter);
		final Long createdId = store.create(_filter);
		final FilterStore.Filter created = store.read(createdId);
		return converter.storeToLogic(created);
	}

	@Override
	public void update(final Filter filter) {
		logger.info(MARKER, "updating filter '{}'", filter);
		final FilterStore.Filter _filter = converter.logicToStore(filter);
		final FilterStore.Filter stored = store.read(_filter.getId());
		final FilterStore.Filter notAllAttributesCanBeUpdated = new ForwardingFilter() {

			@Override
			protected FilterStore.Filter delegate() {
				return stored;
			}

			@Override
			public String getName() {
				return _filter.getName();
			}

			@Override
			public String getDescription() {
				return _filter.getDescription();
			}

			@Override
			public String getClassName() {
				return _filter.getClassName();
			}

			@Override
			public String getConfiguration() {
				return _filter.getConfiguration();
			}

			@Override
			public Long getUserId() {
				/*
				 * if shared updates the user, else keeps the already stored one
				 */
				return isShared() ? _filter.getUserId() : super.getUserId();
			}

		};
		store.update(notAllAttributesCanBeUpdated);
	}

	@Override
	public void delete(final Filter filter) {
		logger.info(MARKER, "deleting filter '{}'", filter);
		final FilterStore.Filter _filter = converter.logicToStore(filter);
		store.delete(_filter);
	}

	@Override
	public PagedElements<Filter> readForCurrentUser(final String className) {
		logger.info(MARKER, "getting all filters for class '{}' for the currently logged user", className);
		final OperationUser operationUser = userStore.getUser();
		final CMUser user = operationUser.getAuthenticatedUser();
		final PagedElements<FilterStore.Filter> userFilters = store.readNonSharedFilters(className, user.getId(), 0,
				MAX_VALUE);
		final PagedElements<org.cmdbuild.services.store.filter.FilterStore.Filter> fetchAllGroupsFilters = store
				.readSharedFilters(className, 0, MAX_VALUE);
		final Iterable<FilterStore.Filter> groupFilters = from(fetchAllGroupsFilters) //
				.filter(new Predicate<FilterStore.Filter>() {

					@Override
					public boolean apply(final FilterStore.Filter input) {
						return (operationUser.hasAdministratorPrivileges() || operationUser.hasReadAccess(input));
					}

				});
		final Iterable<FilterStore.Filter> allFilters = concat(userFilters, groupFilters);
		return new PagedElements<Filter>( //
				from(allFilters) //
						.transform(toLogic()), //
				0);
	}

	@Override
	public PagedElements<Filter> readShared(final String className, final int start, final int limit) {
		logger.info(MARKER, "getting all filters starting from '{}' and with a limit of '{}'", start, limit);
		final PagedElements<FilterStore.Filter> response = store.readSharedFilters(className, start, limit);
		return new PagedElements<Filter>(
				from(response) //
						.transform(toLogic()), //
				response.totalSize());
	}

	@Override
	public PagedElements<Filter> readNotShared(final String className, final int start, final int limit) {
		logger.info(MARKER, "getting all filters for class '{}' starting from '{}' and with a limit of '{}'", className,
				start, limit);
		final PagedElements<FilterStore.Filter> response = store.readNonSharedFilters(className, null, start, limit);
		return new PagedElements<Filter>(
				from(response) //
						.transform(toLogic()), //
				response.totalSize());
	}

	@Override
	public Iterable<Filter> getDefaults(final String className, final String groupName) {
		logger.info(MARKER, "getting first default filter for class '{}' that it's related with group '{}'", className,
				groupName);
		final String _groupName;
		if (groupName == null) {
			_groupName = userStore.getUser().getPreferredGroup().getName();
		} else {
			_groupName = groupName;
		}
		return from(store.read(className, _groupName)) //
				.transform(toLogic());
	}

	@Override
	public void setDefault(final Iterable<Long> filters, final Iterable<String> groups) {
		logger.info(MARKER, "setting default filter '{}' for groups '{}'", filters, groups);
		stream(filters.spliterator(), false) //
				.forEach(filterId -> {
					final FilterStore.Filter filter = store.read(filterId);
					final MapDifference<String, String> difference = difference(uniqueIndex(groups, identity()),
							uniqueIndex(store.joined(filterId), identity()));
					difference.entriesOnlyOnRight().keySet().stream() //
							.forEach(group -> store.disjoin(group, store.read(filter.getClassName(), group)));
					difference.entriesOnlyOnLeft().keySet().stream() //
							.forEach(group -> store.join(group, newArrayList(filter)));
				});
	}

	@Override
	public Iterable<String> getGroups(final Long filter) {
		logger.info(MARKER, "getting groups which filter '{}' is default", filter);
		return store.joined(filter);
	}

	private Function<FilterStore.Filter, Filter> toLogic() {
		return new Function<FilterStore.Filter, Filter>() {

			@Override
			public Filter apply(final FilterStore.Filter input) {
				return converter.storeToLogic(input);
			}

		};
	}

}
