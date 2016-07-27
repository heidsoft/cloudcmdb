package org.cmdbuild.data.store.dao;

import static org.cmdbuild.data.store.dao.DataViewStore.DEFAULT_IDENTIFIER_ATTRIBUTE_NAME;

import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.logic.data.Utils;
import org.slf4j.Logger;

public abstract class BaseStorableConverter<T extends Storable> implements StorableConverter<T> {

	protected static Logger logger = DataViewStore.logger;

	private static String SYSTEM_USER = "system"; // FIXME

	@Override
	public String getIdentifierAttributeName() {
		return DEFAULT_IDENTIFIER_ATTRIBUTE_NAME;
	}

	@Override
	public Storable storableOf(final CMCard card) {
		return new Storable() {

			@Override
			public String getIdentifier() {
				final String attributeName = getIdentifierAttributeName();
				final String value;
				if (DEFAULT_IDENTIFIER_ATTRIBUTE_NAME.equals(attributeName)) {
					value = Long.toString(card.getId());
				} else {
					value = card.get(getIdentifierAttributeName(), String.class);
				}
				return value;
			}

		};
	}

	@Override
	public CMCardDefinition fill(final CMCardDefinition card, final T storable) {
		final Map<String, Object> values = getValues(storable);
		for (final Entry<String, Object> entry : values.entrySet()) {
			logger.debug("setting attribute '{}' with value '{}'", entry.getKey(), entry.getValue());
			card.set(entry.getKey(), entry.getValue());
		}
		return card;
	}

	@Override
	public String getUser(final T storable) {
		return SYSTEM_USER;
	};

	/**
	 * @deprecated use static methods directly instead
	 */
	@Deprecated
	protected String readStringAttribute(final CMCard card, final String attributeName) {
		return Utils.readString(card, attributeName);
	}

}