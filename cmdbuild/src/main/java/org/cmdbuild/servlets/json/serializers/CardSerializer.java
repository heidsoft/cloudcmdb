package org.cmdbuild.servlets.json.serializers;

import static org.cmdbuild.servlets.json.CommunicationConstants.BEGIN_DATE;
import static org.cmdbuild.servlets.json.CommunicationConstants.CLASS_ID_CAPITAL;
import static org.cmdbuild.servlets.json.CommunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID_CAPITAL;
import static org.cmdbuild.servlets.json.CommunicationConstants.RESULTS;
import static org.cmdbuild.servlets.json.CommunicationConstants.ROWS;
import static org.cmdbuild.servlets.json.CommunicationConstants.USER;

import java.util.Map;

import org.cmdbuild.dao.constants.Cardinality;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entry.LookupValue;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.query.clause.QueryDomain.Source;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetRelationList.DomainInfo;
import org.cmdbuild.logic.commands.GetRelationList.DomainWithSource;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.SystemDataAccessLogicBuilder;
import org.cmdbuild.model.data.Card;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Maps;

public class CardSerializer extends AbstractJsonResponseSerializer{

	private final DataAccessLogic dataAccessLogic;
	private final RelationAttributeSerializer relationAttributeSerializer;
	private final LookupSerializer lookupSerializer;

	public CardSerializer(final SystemDataAccessLogicBuilder dataAccessLogicBuilder,
			final RelationAttributeSerializer relationAttributeSerializer, final LookupSerializer lookupSerializer) {
		this.dataAccessLogic = dataAccessLogicBuilder.build();
		this.relationAttributeSerializer = relationAttributeSerializer;
		this.lookupSerializer = lookupSerializer;
	}

	/*
	 * TODO continue the implementation, pay attention to lookup and references
	 */

	public JSONObject toClient(final Card card, final String wrapperLabel) throws JSONException {
		final JSONObject json = new JSONObject();

		// add the attributes
		for (final Map.Entry<String, Object> entry : card.getAttributes().entrySet()) {
			final Object output;
			final Object input = entry.getValue();

			if (input instanceof IdAndDescription) {

				if (input instanceof LookupValue) {
					output = lookupSerializer.serializeLookupValue((LookupValue) input);
				} else {
					final IdAndDescription idAndDescription = IdAndDescription.class.cast(input);
					final Map<String, Object> map = Maps.newHashMap();
					map.put(ID, idAndDescription.getId());
					map.put(DESCRIPTION, idAndDescription.getDescription());
					output = map;
				}

			} else {
				output = entry.getValue();
			}

			json.put(entry.getKey(), output);
		}

		// add some required info
		json.put(ID_CAPITAL, card.getId());
		// TODO if IdClass is no more needed, remove getClassId() method too
		json.put(CLASS_ID_CAPITAL, card.getClassId());

		json.put(CLASS_NAME, card.getClassName());
		json.put(BEGIN_DATE, formatDateTime(card.getBeginDate()));
		json.put(USER, card.getUser());

		/*
		 * We must serialize the class description because it is used while listing
		 * the card of a superclass to show the actual class. The ugly key is
		 * driven by backward compatibility
		 */
		json.put("IdClass_value", card.getClassDescription());

		// wrap in a JSON object if required
		final JSONObject output;
		if (wrapperLabel != null) {
			final JSONObject wrapper = new JSONObject();
			wrapper.put(wrapperLabel, json);
			wrapper.put("referenceAttributes", getReferenceAttributes(card));
			output = wrapper;
		} else {
			output = json;
		}
		return output;
	}

	/*
	 * Return a map with the reference attribute names as keys and a map with
	 * name-value of the relation attributes
	 */
	private Map<String, JSONObject> getReferenceAttributes(final Card card) {
		final Map<String, JSONObject> referenceAttributes = Maps.newHashMap();
		final CMClass owner = card.getType();
		if (owner == null) {
			return referenceAttributes;
		}

		for (final String referenceAttributeName : card.getAttributes().keySet()) {
			final CMAttributeType<?> attributeType = owner.getAttribute(referenceAttributeName).getType();
			if (attributeType instanceof ReferenceAttributeType) {
				final String domainName = ((ReferenceAttributeType) attributeType).getDomainName();
				final Long domainId = dataAccessLogic.findDomain(domainName).getId();
				final GetRelationListResponse response;
				if (dataAccessLogic.findDomain(domainName).getCardinality().equals(Cardinality.CARDINALITY_1N.value())) {
					response = dataAccessLogic.getRelationList(card,
							DomainWithSource.create(domainId, Source._2.toString()));
				} else { // CARDINALITY_N1
					response = dataAccessLogic.getRelationList(card,
							DomainWithSource.create(domainId, Source._1.toString()));
				}

				for (final DomainInfo domainInfo : response) {
					for (final RelationInfo relationInfo : domainInfo) {
						referenceAttributes.put(referenceAttributeName,
								relationAttributeSerializer.toClient(relationInfo, true));
					}
				}
			}
		}
		return referenceAttributes;
	}

	public JSONObject toClient(final Card card) throws JSONException {
		return toClient(card, null);
	}

	public JSONObject toClient( //
			final Iterable<Card> cards, //
			final int totalSize //
	) throws JSONException {

		return toClient(cards, totalSize, ROWS);
	}

	public JSONObject toClient( //
			final Iterable<Card> cards, //
			final int totalSize, //
			final String cardsLabel //
	) throws JSONException {

		final JSONObject json = new JSONObject();
		final JSONArray jsonRows = new JSONArray();
		for (final Card card : cards) {
			jsonRows.put(toClient(card));
		}

		json.put(cardsLabel, jsonRows);
		json.put(RESULTS, totalSize);
		return json;
	}

}
