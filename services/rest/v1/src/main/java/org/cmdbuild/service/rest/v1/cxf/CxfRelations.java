package org.cmdbuild.service.rest.v1.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.addAll;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.transformEntries;
import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.cmdbuild.logic.mapping.json.Constants.FilterOperator.EQUAL;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.ATTRIBUTE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.OPERATOR_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.VALUE_KEY;
import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_DESTINATION_ID;
import static org.cmdbuild.service.rest.v1.constants.Serialization.UNDERSCORED_SOURCE_ID;
import static org.cmdbuild.service.rest.v1.cxf.util.Json.safeJsonObject;
import static org.cmdbuild.service.rest.v1.model.Models.newCard;
import static org.cmdbuild.service.rest.v1.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v1.model.Models.newRelation;
import static org.cmdbuild.service.rest.v1.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v1.model.Models.newResponseSingle;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetRelationList.DomainInfo;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.RelationDTO;
import org.cmdbuild.logic.mapping.json.JsonFilterHelper;
import org.cmdbuild.logic.mapping.json.JsonFilterHelper.FilterElementGetter;
import org.cmdbuild.service.rest.v1.Relations;
import org.cmdbuild.service.rest.v1.cxf.serialization.DefaultConverter;
import org.cmdbuild.service.rest.v1.model.Relation;
import org.cmdbuild.service.rest.v1.model.ResponseMultiple;
import org.cmdbuild.service.rest.v1.model.ResponseSingle;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Maps.EntryTransformer;

public class CxfRelations implements Relations {

	private static class RelationInfoToRelation implements Function<RelationInfo, Relation> {

		public static class Builder implements org.apache.commons.lang3.builder.Builder<RelationInfoToRelation> {

			private boolean includeValues;

			private Builder() {
				// use factory method
			}

			@Override
			public RelationInfoToRelation build() {
				return new RelationInfoToRelation(this);
			}

			public Builder includeValues(final boolean includeValues) {
				this.includeValues = includeValues;
				return this;
			}

		}

		public static Builder newInstance() {
			return new Builder();
		}

		private static final Map<String, Object> NO_VALUES = emptyMap();

		private final boolean includeValues;

		private RelationInfoToRelation(final Builder builder) {
			this.includeValues = builder.includeValues;
		}

		@Override
		public Relation apply(final RelationInfo input) {
			final CMDomain domain = input.getQueryDomain().getDomain();
			final CMClass sourceType = input.getSourceType();
			final CMClass targetType = input.getTargetType();
			return newRelation() //
					.withId(input.getRelationId()) //
					.withType(domain.getName()) //
					.withSource(newCard() //
							.withType(sourceType.getName()) //
							.withId(input.getSourceId()) //
							.withValue(sourceType.getDescriptionAttributeName(), input.getSourceDescription()) //
							.build()) //
					.withDestination(newCard() //
							.withType(targetType.getName()) //
							.withId(input.getTargetId()) //
							.withValue(targetType.getDescriptionAttributeName(), input.getTargetDescription()) //
							.build()) //
					.withValues(includeValues ? adaptOutputValues( //
							domain, //
							input.getRelationAttributes() //
					)
							: NO_VALUES.entrySet()) //
					.build();
		}

		private Iterable<? extends Entry<String, Object>> adaptOutputValues(final CMEntryType target,
				final Iterable<Entry<String, Object>> entries) {
			final Map<String, Object> values = newHashMap();
			for (final Entry<String, Object> entry : entries) {
				values.put(entry.getKey(), entry.getValue());
			}
			return transformEntries(values, new EntryTransformer<String, Object, Object>() {

				@Override
				public Object transformEntry(final String key, final Object value) {
					final CMAttribute attribute = target.getAttribute(key);
					final Object _value;
					if (attribute == null) {
						_value = value;
					} else {
						final CMAttributeType<?> attributeType = attribute.getType();
						_value = DefaultConverter.newInstance() //
								.build() //
								.toClient() //
								.convert(attributeType, value);
					}
					return _value;
				}

			}).entrySet();
		}

	};

	private static final RelationInfoToRelation BASIC_DETAILS = RelationInfoToRelation.newInstance() //
			.includeValues(false) //
			.build();

	private static final RelationInfoToRelation FULL_DETAILS = RelationInfoToRelation.newInstance() //
			.includeValues(true) //
			.build();

	private final ErrorHandler errorHandler;
	private final DataAccessLogic dataAccessLogic;

	public CxfRelations(final ErrorHandler errorHandler, final DataAccessLogic dataAccessLogic) {
		this.errorHandler = errorHandler;
		this.dataAccessLogic = dataAccessLogic;
	}

	@Override
	public ResponseSingle<Long> create(final String domainId, final Relation relation) {
		final CMDomain targetDomain = dataAccessLogic.findDomain(domainId);
		if (targetDomain == null) {
			errorHandler.domainNotFound(domainId);
		}
		try {
			final RelationDTO relationDTO = relationDto(targetDomain, relation);
			final Long created = from(dataAccessLogic.createRelations(relationDTO)).first().get();
			return newResponseSingle(Long.class) //
					.withElement(created) //
					.build();
		} catch (final Exception e) {
			errorHandler.propagate(e);
		}
		return null;
	}

	@Override
	public ResponseMultiple<Relation> read(final String domainId, final String filter, final Integer limit,
			final Integer offset, final boolean detailed) {
		final CMDomain targetDomain = dataAccessLogic.findDomain(domainId);
		if (targetDomain == null) {
			errorHandler.domainNotFound(domainId);
		}
		try {
			String _filter = defaultString(filter);
			// TODO do it better
			// <<<<<
			final String regex_1 = "\"attribute\"[\\w]*:[\\w]*\"" + UNDERSCORED_SOURCE_ID + "\"";
			final String replacement_1 = "\"attribute\":\"IdObj1\"";
			_filter = _filter.replaceAll(regex_1, replacement_1);

			final String regex_2 = "\"attribute\"[\\w]*:[\\w]*\"" + UNDERSCORED_DESTINATION_ID + "\"";
			final String replacement_2 = "\"attribute\":\"IdObj2\"";
			_filter = _filter.replaceAll(regex_2, replacement_2);
			// <<<<<
			final QueryOptions queryOptions = QueryOptions.newQueryOption() //
					.filter(new JsonFilterHelper(safeJsonObject(_filter)).merge(new FilterElementGetter() {

						@Override
						public boolean hasElement() {
							return true;
						}

						@Override
						public JSONObject getElement() throws JSONException {
							final JSONArray jsonValues = new JSONArray();
							jsonValues.put("_1");

							final JSONObject jsonObject = new JSONObject();
							jsonObject.put(ATTRIBUTE_KEY, "_Src");
							jsonObject.put(OPERATOR_KEY, EQUAL);
							jsonObject.put(VALUE_KEY, jsonValues);

							return jsonObject;
						}
					})) //
					.limit(limit) //
					.offset(offset) //
					.build();
			final GetRelationListResponse response = dataAccessLogic.getRelationList(targetDomain, queryOptions);
			final List<Relation> elements = newArrayList();
			for (final DomainInfo domainInfo : response) {
				addAll(elements, from(domainInfo) //
						.transform(detailed ? FULL_DETAILS : BASIC_DETAILS));
			}
			return newResponseMultiple(Relation.class) //
					.withElements(elements) //
					.withMetadata(newMetadata() //
							.withTotal(Long.valueOf(response.getTotalNumberOfRelations())) //
							.build()) //
					.build();
		} catch (final Exception e) {
			errorHandler.propagate(e);
		}
		return null;
	}

	@Override
	public ResponseSingle<Relation> read(final String domainId, final Long relationId) {
		final CMDomain targetDomain = dataAccessLogic.findDomain(domainId);
		if (targetDomain == null) {
			errorHandler.domainNotFound(domainId);
		}
		final Optional<RelationInfo> relation = getRelation(relationId, targetDomain);
		if (!relation.isPresent()) {
			errorHandler.relationNotFound(relationId);
		}
		final Relation element = FULL_DETAILS.apply(relation.get());
		return newResponseSingle(Relation.class) //
				.withElement(element) //
				.build();
	}

	private Optional<RelationInfo> getRelation(final Long relationId, final CMDomain targetDomain) {
		try {
			return dataAccessLogic.getRelation(targetDomain, relationId);
		} catch (final Exception e) {
			errorHandler.propagate(e);
		}
		return Optional.absent();
	}

	@Override
	public void update(final String domainId, final Long relationId, final Relation relation) {
		final CMDomain targetDomain = dataAccessLogic.findDomain(domainId);
		if (targetDomain == null) {
			errorHandler.domainNotFound(domainId);
		}
		try {
			final RelationDTO relationDTO = relationDto(targetDomain, relation);
			dataAccessLogic.updateRelation(relationDTO);
		} catch (final Exception e) {
			errorHandler.propagate(e);
		}
	}

	@Override
	public void delete(final String domainId, final Long relationId) {
		final CMDomain targetDomain = dataAccessLogic.findDomain(domainId);
		if (targetDomain == null) {
			errorHandler.domainNotFound(domainId);
		}
		try {
			dataAccessLogic.deleteRelation(domainId, relationId);
		} catch (final Exception e) {
			errorHandler.propagate(e);
		}
	}

	private RelationDTO relationDto(final CMDomain domain, final Relation relation) {
		final RelationDTO relationDTO = new RelationDTO();
		relationDTO.relationId = relation.getId();
		relationDTO.domainName = domain.getName();
		relationDTO.master = "_1";
		relationDTO.addSourceCard(relation.getSource().getId(), relation.getSource().getType());
		relationDTO.addDestinationCard(relation.getDestination().getId(), relation.getDestination().getType());
		relationDTO.relationAttributeToValue = relation.getValues();
		return relationDTO;
	}

}
