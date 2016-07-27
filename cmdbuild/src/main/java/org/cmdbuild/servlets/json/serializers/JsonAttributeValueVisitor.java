package org.cmdbuild.servlets.json.serializers;

import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;

import java.util.Map;

import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entry.LookupValue;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.logic.translation.TranslationFacade;

import com.google.common.collect.Maps;

public class JsonAttributeValueVisitor extends AbstractAttributeValueVisitor {

	public JsonAttributeValueVisitor(final CMAttributeType<?> type, final Object value,
			final TranslationFacade translationFacade, final LookupSerializer lookupSerializer) {
		super(type, value, translationFacade, lookupSerializer);
	}

	@Override
	public void visit(final EntryTypeAttributeType attributeType) {
		convertedValue = value;
	}

	@Override
	public void visit(final LookupAttributeType attributeType) {
		if (value instanceof IdAndDescription) {
			if (value instanceof LookupValue) {
				convertedValue = lookupSerializer.serializeLookupValue((LookupValue) value);
			} else {
				convertedValue = asMap((IdAndDescription) value);
			}
		} else {
			convertedValue = value;
		}
	}

	@Override
	public void visit(final ReferenceAttributeType attributeType) {
		if (value instanceof IdAndDescription) {
			convertedValue = asMap((IdAndDescription) value);
		} else {
			convertedValue = value;
		}
	}

	private Object asMap(final IdAndDescription value) {
		final Map<String, Object> map = Maps.newHashMap();
		map.put(ID, value.getId());
		map.put(DESCRIPTION, value.getDescription());

		return map;
	}
}