package org.cmdbuild.logic.taskmanager.util;

import static java.util.Arrays.asList;
import static org.cmdbuild.logic.mapping.json.Constants.FilterOperator.EQUAL;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.ATTRIBUTE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.OPERATOR_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.VALUE_KEY;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.logic.mapping.json.JsonFilterHelper.FilterElementGetter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CardIdFilterElementGetter implements FilterElementGetter {

	public static CardIdFilterElementGetter of(final CMCard card) {
		return new CardIdFilterElementGetter(card);
	}

	private final CMCard card;

	private CardIdFilterElementGetter(final CMCard card) {
		this.card = card;
	}

	@Override
	public boolean hasElement() {
		return true;
	}

	@Override
	public JSONObject getElement() throws JSONException {
		logger.debug(marker, "creating JSON element for '{}'", card.getId());
		final JSONObject element = new JSONObject();
		element.put(ATTRIBUTE_KEY, "Id");
		element.put(OPERATOR_KEY, EQUAL);
		element.put(VALUE_KEY, new JSONArray(asList(card.getId())));
		logger.debug(marker, "resulting element is '{}'", element);
		return element;
	}

}