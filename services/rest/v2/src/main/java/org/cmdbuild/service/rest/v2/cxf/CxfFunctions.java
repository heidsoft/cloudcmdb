package org.cmdbuild.service.rest.v2.cxf;

import static com.google.common.base.Predicates.alwaysTrue;
import static com.google.common.base.Predicates.equalTo;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Ordering.from;
import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cmdbuild.dao.entrytype.Predicates.functionId;
import static org.cmdbuild.dao.guava.Functions.toValueSet;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.alias.Aliases.name;
import static org.cmdbuild.service.rest.v2.model.Models.newAttribute;
import static org.cmdbuild.service.rest.v2.model.Models.newFunctionWithBasicDetails;
import static org.cmdbuild.service.rest.v2.model.Models.newFunctionWithFullDetails;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;
import static org.cmdbuild.service.rest.v2.model.Models.newValues;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.function.CMFunction.CMFunctionParameter;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.clause.Clauses;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.access.filter.json.JsonParser;
import org.cmdbuild.logic.data.access.filter.model.Element;
import org.cmdbuild.logic.data.access.filter.model.Filter;
import org.cmdbuild.logic.data.access.filter.model.Parser;
import org.cmdbuild.service.rest.v2.Functions;
import org.cmdbuild.service.rest.v2.cxf.filter.FunctionElementPredicate;
import org.cmdbuild.service.rest.v2.cxf.serialization.AttributeTypeResolver;
import org.cmdbuild.service.rest.v2.cxf.serialization.Converter.ValueConverter;
import org.cmdbuild.service.rest.v2.cxf.serialization.DefaultConverter;
import org.cmdbuild.service.rest.v2.model.Attribute;
import org.cmdbuild.service.rest.v2.model.FunctionWithBasicDetails;
import org.cmdbuild.service.rest.v2.model.FunctionWithFullDetails;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.cmdbuild.service.rest.v2.model.Values;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class CxfFunctions implements Functions {

	private static final Comparator<CMFunction> ID_ASC = new Comparator<CMFunction>() {

		@Override
		public int compare(final CMFunction o1, final CMFunction o2) {
			return o1.getId().compareTo(o2.getId());
		}

	};

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final Map<String, Object> NO_VALUES = emptyMap();
	private static final Alias F = name("f");

	private final ErrorHandler errorHandler;
	/**
	 * @deprecated enclose within a logic
	 */
	@Deprecated
	private final CMDataView dataView;

	public CxfFunctions(final ErrorHandler errorHandler, final CMDataView dataView) {
		this.errorHandler = errorHandler;
		this.dataView = dataView;
	}

	@Override
	public ResponseMultiple<FunctionWithBasicDetails> readAll(final Integer limit, final Integer offset,
			final String filter) {
		final Predicate<CMFunction> predicate;
		if (isNotBlank(filter)) {
			final Parser parser = new JsonParser(filter);
			final Filter filterModel = parser.parse();
			final Optional<Element> element = filterModel.attribute();
			if (element.isPresent()) {
				predicate = new FunctionElementPredicate(element.get());
			} else {
				predicate = alwaysTrue();
			}
		} else {
			predicate = alwaysTrue();
		}
		final Iterable<? extends CMFunction> all = dataView.findAllFunctions();
		final Iterable<? extends CMFunction> ordered = from(ID_ASC).sortedCopy(all);
		final Iterable<? extends CMFunction> filtered = from(ordered) //
				.filter(predicate);
		final Iterable<FunctionWithBasicDetails> elements = from(filtered) //
				.skip((offset == null) ? 0 : offset) //
				.limit((limit == null) ? Integer.MAX_VALUE : limit) //
				.transform(new Function<CMFunction, FunctionWithBasicDetails>() {

					@Override
					public FunctionWithBasicDetails apply(final CMFunction input) {
						return newFunctionWithBasicDetails() //
								.withId(input.getId()) //
								.withName(input.getName()) //
								.withDescription(input.getName()) //
								.build();
					}

				});
		return newResponseMultiple(FunctionWithBasicDetails.class) //
				.withElements(elements) //
				.withMetadata(newMetadata() //
						.withTotal(size(filtered)) //
						.build()) //
				.build();
	}

	@Override
	public ResponseSingle<FunctionWithFullDetails> read(final Long functionId) {
		final Optional<? extends CMFunction> function = from(dataView.findAllFunctions()) //
				.filter(functionId(equalTo(functionId))) //
				.first();
		if (!function.isPresent()) {
			errorHandler.functionNotFound(functionId);
		}
		final FunctionWithFullDetails element = new Function<CMFunction, FunctionWithFullDetails>() {

			@Override
			public FunctionWithFullDetails apply(final CMFunction input) {
				return newFunctionWithFullDetails() //
						.withId(input.getId()) //
						.withName(input.getName()) //
						.withDescription(input.getName()) //
						.build();
			}

		}.apply(function.get());
		return newResponseSingle(FunctionWithFullDetails.class) //
				.withElement(element) //
				.build();
	}

	@Override
	public ResponseMultiple<Attribute> readInputParameters(final Long functionId, final Integer limit,
			final Integer offset) {
		final Optional<? extends CMFunction> function = from(dataView.findAllFunctions()) //
				.filter(functionId(equalTo(functionId))) //
				.first();
		if (!function.isPresent()) {
			errorHandler.functionNotFound(functionId);
		}
		final Iterable<CMFunctionParameter> parameters = function.get().getInputParameters();
		return serialize(parameters, limit, offset);
	}

	@Override
	public ResponseMultiple<Attribute> readOutputParameters(final Long functionId, final Integer limit,
			final Integer offset) {
		final Optional<? extends CMFunction> function = from(dataView.findAllFunctions()) //
				.filter(functionId(equalTo(functionId))) //
				.first();
		if (!function.isPresent()) {
			errorHandler.functionNotFound(functionId);
		}
		final Iterable<CMFunctionParameter> parameters = function.get().getOutputParameters();
		return serialize(parameters, limit, offset);
	}

	private ResponseMultiple<Attribute> serialize(final Iterable<CMFunctionParameter> parameters, final Integer limit,
			final Integer offset) {
		final Iterable<Attribute> elements = from(parameters) //
				.skip((offset == null) ? 0 : offset) //
				.limit((limit == null) ? Integer.MAX_VALUE : limit) //
				.transform(new Function<CMFunctionParameter, Attribute>() {

					private long index = 0;

					@Override
					public Attribute apply(final CMFunctionParameter input) {
						return newAttribute() //
								.withId(input.getName()) //
								.withName(input.getName()) //
								.withDescription(input.getName()) //
								.withType(new AttributeTypeResolver().resolve(input.getType()).asString()) //
								.thatIsActive(true) //
								.withIndex(index++) //
								.build();
					}

				});
		return newResponseMultiple(Attribute.class) //
				.withElements(elements) //
				.withMetadata(newMetadata() //
						.withTotal(size(parameters)) //
						.build()) //
				.build();
	}

	@Override
	public ResponseMultiple<Values> call(final Long functionId, final String inputs) {
		final Optional<? extends CMFunction> _function = from(dataView.findAllFunctions()) //
				.filter(functionId(equalTo(functionId))) //
				.first();
		if (!_function.isPresent()) {
			errorHandler.functionNotFound(functionId);
		}
		final CMFunction function = _function.get();
		final Object[] actualParams = convertInputs(function, parse(inputs));
		final CMQueryResult result = dataView.select(anyAttribute(function, F)) //
				.from(Clauses.call(function, actualParams), F) //
				.run();
		final Iterable<Values> elements = from(result) //
				.transform(toValueSet(F)) //
				.transform(new Function<CMValueSet, Map<String, Object>>() {

					@Override
					public Map<String, Object> apply(final CMValueSet input) {
						final Map<String, Object> output = newHashMap();
						for (final Entry<String, Object> element : input.getValues()) {
							output.put(element.getKey(), element.getValue());
						}
						return output;
					}

				}) //
				.transform(new Function<Map<String, Object>, Values>() {

					@Override
					public Values apply(final Map<String, Object> input) {
						return newValues() //
								.withValues(input) //
								.build();
					}

				}) //
				.transform(new Function<Values, Values>() {

					private final ValueConverter converter = DefaultConverter.newInstance() //
							.build() //
							.toClient();

					@Override
					public Values apply(final Values input) {
						for (final CMFunction.CMFunctionParameter element : function.getOutputParameters()) {
							final String name = element.getName();
							final CMAttributeType<?> type = element.getType();
							final Object value = input.get(name);
							final Object converted = converter.convert(type, value);
							input.put(name, converted);
						}
						return input;
					}

				});
		return newResponseMultiple(Values.class) //
				.withElements(elements) //
				.withMetadata(newMetadata() //
						.withTotal(size(elements)) //
						.build()) //
				.build();
	}

	private Map<String, Object> parse(final String value) {
		try {
			final Map<String, Object> output;
			if (isNotBlank(value)) {
				output = OBJECT_MAPPER.readValue(value, Map.class);
			} else {
				output = NO_VALUES;
			}
			return output;
		} catch (final Exception e) {
			errorHandler.propagate(e);
			throw new AssertionError("should not come here");
		}
	}

	private static Object[] convertInputs(final CMFunction function, final Map<String, Object> inputs) {
		final Collection<Object> params = newArrayList();
		for (final CMFunctionParameter element : function.getInputParameters()) {
			params.add(inputs.get(element.getName()));
		}
		return params.toArray();
	}

}
