package org.cmdbuild.data.store.translation;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.cmdbuild.data.store.translation.Constants.CLASS_NAME;
import static org.cmdbuild.data.store.translation.Constants.ELEMENT;
import static org.cmdbuild.data.store.translation.Constants.LANG;
import static org.cmdbuild.data.store.translation.Constants.VALUE;

import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.dao.BaseStorableConverter;

import com.google.common.collect.Maps;

public class TranslationConverter extends BaseStorableConverter<Translation> {

	@Override
	public String getClassName() {
		return CLASS_NAME;
	}

	@Override
	public Translation convert(final CMCard card) {
		final Translation translation = new Translation(card.getId());
		translation.setElement(defaultIfBlank(card.get(ELEMENT, String.class), null));
		translation.setLang(defaultIfBlank(card.get(LANG, String.class), null));
		translation.setValue(defaultIfBlank(card.get(VALUE, String.class), null));
		return translation;
	}

	@Override
	public Map<String, Object> getValues(final Translation storable) {
		final Map<String, Object> values = Maps.newHashMap();
		values.put(ELEMENT, storable.getElement());
		values.put(LANG, storable.getLang());
		values.put(VALUE, storable.getValue());
		return values;
	}

}
