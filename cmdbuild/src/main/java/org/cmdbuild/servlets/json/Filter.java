package org.cmdbuild.servlets.json;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.cmdbuild.services.json.dto.JsonResponse.success;
import static org.cmdbuild.servlets.json.CommunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.CONFIGURATION;
import static org.cmdbuild.servlets.json.CommunicationConstants.COUNT;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.ELEMENTS;
import static org.cmdbuild.servlets.json.CommunicationConstants.ENTRY_TYPE;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILTER;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILTERS;
import static org.cmdbuild.servlets.json.CommunicationConstants.GROUP;
import static org.cmdbuild.servlets.json.CommunicationConstants.GROUPS;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.LIMIT;
import static org.cmdbuild.servlets.json.CommunicationConstants.NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.START;
import static org.cmdbuild.servlets.json.CommunicationConstants.TEMPLATE;
import static org.cmdbuild.servlets.json.schema.Utils.toIterable;
import static org.cmdbuild.servlets.json.schema.Utils.JsonParser.AS_LONG;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.logic.filter.FilterLogic;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Function;

public class Filter extends JSONBaseWithSpringContext {

	private static class FilterImpl implements FilterLogic.Filter {

		private static class Builder implements org.apache.commons.lang3.builder.Builder<FilterImpl> {

			private Long id;
			private String name;
			private String description;
			private String className;
			private String configuration;
			private boolean shared;

			/**
			 * Use factory method.
			 */
			private Builder() {
			}

			@Override
			public FilterImpl build() {
				return new FilterImpl(this);
			}

			public Builder withId(final Long id) {
				this.id = id;
				return this;
			}

			public Builder withName(final String name) {
				this.name = name;
				return this;
			}

			public Builder withDescription(final String description) {
				this.description = description;
				return this;
			}

			public Builder withClassName(final String className) {
				this.className = className;
				return this;
			}

			public Builder withConfiguration(final String configuration) {
				this.configuration = configuration;
				return this;
			}

			public Builder thatIsShared(final boolean shared) {
				this.shared = shared;
				return this;
			}

		}

		public static Builder newInstance() {
			return new Builder();
		}

		private final Long id;
		private final String name;
		private final String description;
		private final String className;
		private final String configuration;
		private final boolean shared;

		private FilterImpl(final Builder builder) {
			this.id = builder.id;
			this.name = builder.name;
			this.description = builder.description;
			this.className = builder.className;
			this.configuration = builder.configuration;
			this.shared = builder.shared;
		}

		@Override
		public Long getId() {
			return id;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public String getClassName() {
			return className;
		}

		@Override
		public String getConfiguration() {
			return configuration;
		}

		@Override
		public boolean isShared() {
			return shared;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof FilterLogic.Filter)) {
				return false;
			}
			final FilterLogic.Filter other = FilterLogic.Filter.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.getId(), other.getId()) //
					.append(this.getName(), other.getName()) //
					.append(this.getDescription(), other.getDescription()) //
					.append(this.getClassName(), other.getClassName()) //
					.append(this.getConfiguration(), other.getConfiguration()) //
					.append(this.isShared(), other.isShared()) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(getId()) //
					.append(getName()) //
					.append(getDescription()) //
					.append(getClassName()) //
					.append(getConfiguration()) //
					.append(isShared()) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	private static class JsonFilter {

		private static final Function<FilterLogic.Filter, JsonFilter> FUNCTION = new Function<FilterLogic.Filter, Filter.JsonFilter>() {

			@Override
			public JsonFilter apply(final FilterLogic.Filter input) {
				return new JsonFilter(input);
			}

		};

		public static Function<FilterLogic.Filter, JsonFilter> function() {
			return FUNCTION;
		}

		private final FilterLogic.Filter delegate;

		public JsonFilter(final FilterLogic.Filter delegate) {
			this.delegate = delegate;
		}

		@JsonProperty(ID)
		public Long getId() {
			return delegate.getId();
		}

		@JsonProperty(NAME)
		public String getName() {
			return delegate.getName();
		}

		@JsonProperty(DESCRIPTION)
		public String getDescription() {
			return delegate.getDescription();
		}

		@JsonProperty(ENTRY_TYPE)
		public String getClassName() {
			return delegate.getClassName();
		}

		@JsonProperty(CONFIGURATION)
		public String getConfiguration() {
			return delegate.getConfiguration();
		}

		@JsonProperty(TEMPLATE)
		public boolean isShared() {
			return delegate.isShared();
		}

	}

	private static class JsonFilters {

		private static class Builder implements org.apache.commons.lang3.builder.Builder<JsonFilters> {

			private List<JsonFilter> elements;

			private Builder() {
				// use factory method
			}

			@Override
			public JsonFilters build() {
				return new JsonFilters(this);
			}

			public Builder withElements(final Iterable<? extends JsonFilter> elements) {
				this.elements = newArrayList(elements);
				return this;
			}

		}

		public static Builder newInstance() {
			return new Builder();
		}

		private final List<JsonFilter> elements;

		private JsonFilters(final Builder builder) {
			this.elements = builder.elements;
		}

		@JsonProperty(ELEMENTS)
		public List<JsonFilter> getElements() {
			return elements;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof JsonFilters)) {
				return false;
			}
			final JsonFilters other = JsonFilters.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.elements, other.elements) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(elements) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	private static class JsonGroups {

		private static class Builder implements org.apache.commons.lang3.builder.Builder<JsonGroups> {

			private List<String> elements;

			private Builder() {
				// use factory method
			}

			@Override
			public JsonGroups build() {
				return new JsonGroups(this);
			}

			public Builder withElements(final Iterable<? extends String> elements) {
				this.elements = newArrayList(elements);
				return this;
			}

		}

		public static Builder newInstance() {
			return new Builder();
		}

		private final List<String> elements;

		private JsonGroups(final Builder builder) {
			this.elements = builder.elements;
		}

		@JsonProperty(ELEMENTS)
		public List<String> getElements() {
			return elements;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof JsonFilters)) {
				return false;
			}
			final JsonFilters other = JsonFilters.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.elements, other.elements) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(elements) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	/**
	 * Retrieves only users' filters (it does not fetches filters defined for
	 * groups)
	 *
	 * @param start
	 *            is the offset (used for pagination)
	 * @param limit
	 *            is the max number of rows for each page (used for pagination)
	 * @return
	 * @throws JSONException
	 * @throws CMDBException
	 */
	@JSONExported
	@Admin
	public JSONObject read( //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = START) final int start, //
			@Parameter(value = LIMIT) final int limit //
	) throws JSONException {
		final PagedElements<FilterLogic.Filter> filters = filterLogic().readNotShared(className, start, limit);
		return serialize(filters);
	}

	/**
	 * Retrieves only groups filters
	 *
	 * @param start
	 *            is the offset (used for pagination)
	 * @param limit
	 *            is the max number of rows for each page (used for pagination)
	 * @return
	 * @throws JSONException
	 * @throws CMDBException
	 */
	@JSONExported
	@Admin
	public JSONObject readAllGroupFilters( //
			@Parameter(value = CLASS_NAME, required = false) final String className, //
			@Parameter(value = START) final int start, //
			@Parameter(value = LIMIT) final int limit //
	) throws JSONException {
		final PagedElements<FilterLogic.Filter> filters = filterLogic().readShared(className, start, limit);
		return serialize(filters);
	}

	/**
	 * Retrieves, for the currently logged user, all filters (group and user
	 * filters) that are referred to the className
	 *
	 * @param className
	 * @return
	 * @throws JSONException
	 */
	@JSONExported
	public JSONObject readForUser( //
			@Parameter(value = CLASS_NAME) final String className //
	) throws JSONException {
		final PagedElements<FilterLogic.Filter> filters = filterLogic().readForCurrentUser(className);
		return serialize(filters);
	}

	@JSONExported
	public JSONObject create( //
			@Parameter(value = NAME) final String name, //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = DESCRIPTION) final String description, //
			@Parameter(value = CONFIGURATION) final JSONObject configuration, //
			@Parameter(value = TEMPLATE, required = false) final boolean template //
	) throws JSONException {
		final FilterLogic.Filter filter = filterLogic().create(FilterImpl.newInstance() //
				.withName(name) //
				.withDescription(description) //
				.withClassName(className) //
				.withConfiguration(configuration.toString()) //
				.thatIsShared(template) //
				.build());
		return serialize(filter, FILTER);
	}

	@JSONExported
	public void update( //
			@Parameter(value = ID) final Long id, //
			@Parameter(value = NAME) final String name, //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = DESCRIPTION) final String description, //
			@Parameter(value = CONFIGURATION) final JSONObject configuration //
	) {
		filterLogic().update(FilterImpl.newInstance() //
				.withId(id) //
				.withName(name) //
				.withDescription(description) //
				.withClassName(className) //
				.withConfiguration(configuration.toString()) //
				.build());
	}

	@JSONExported
	public void delete( //
			@Parameter(value = ID) final Long id //
	) {
		filterLogic().delete(FilterImpl.newInstance() //
				.withId(id) //
				.build());
	}

	@JSONExported
	public JsonResponse getDefault( //
			@Parameter(value = CLASS_NAME, required = false) final String className, //
			@Parameter(value = GROUP, required = false) final String groupName //
	) {
		final Iterable<FilterLogic.Filter> element = filterLogic().getDefaults(className, groupName);
		return success(JsonFilters.newInstance() //
				.withElements(from(element) //
						.transform(JsonFilter.function())) //
				.build());
	}

	@JSONExported
	@Admin
	public void setDefault( //
			@Parameter(value = FILTERS) final JSONArray filters, //
			@Parameter(value = GROUPS) final JSONArray groups //
	) {
		final Iterable<Long> _filters = toIterable(filters, AS_LONG);
		final Iterable<String> _groups = toIterable(groups);
		filterLogic().setDefault(_filters, _groups);
	}

	@JSONExported
	public JsonResponse getGroups( //
			@Parameter(value = ID) final Long filter //
	) {
		final Iterable<String> elements = filterLogic().getGroups(filter);
		return success(JsonGroups.newInstance() //
				.withElements(elements) //
				.build());
	}

	/**
	 * @deprecated do it using JSON mapping.
	 */
	@Deprecated
	private static JSONObject serialize(final PagedElements<FilterLogic.Filter> filters) throws JSONException {
		final JSONArray jsonFilters = new JSONArray();
		for (final FilterLogic.Filter f : filters) {
			jsonFilters.put(serialize(f));
		}
		final JSONObject out = new JSONObject();
		out.put(FILTERS, jsonFilters);
		out.put(COUNT, filters.totalSize());
		return out;
	}

	/**
	 * @deprecated do it using JSON mapping.
	 */
	@Deprecated
	private static JSONObject serialize(final FilterLogic.Filter filter) throws JSONException {
		return serialize(filter, null);
	}

	/**
	 * @deprecated do it using JSON mapping.
	 */
	@Deprecated
	private static JSONObject serialize(final FilterLogic.Filter filter, final String wrapperName) throws JSONException {
		final JSONObject jsonFilter = new JSONObject();
		jsonFilter.put(ID, filter.getId());
		jsonFilter.put(NAME, filter.getName());
		jsonFilter.put(DESCRIPTION, filter.getDescription());
		jsonFilter.put(ENTRY_TYPE, filter.getClassName());
		jsonFilter.put(TEMPLATE, filter.isShared());
		jsonFilter.put(CONFIGURATION, new JSONObject(filter.getConfiguration()));
		final JSONObject out;
		if (wrapperName != null) {
			out = new JSONObject();
			out.put(wrapperName, jsonFilter);
		} else {
			out = jsonFilter;
		}
		return out;
	}

}
