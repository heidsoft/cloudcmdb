package org.cmdbuild.services.store.filter;

import static com.google.common.collect.Maps.newHashMap;
import static org.cmdbuild.services.store.filter.DataViewFilterStore.CLASS_ID;
import static org.cmdbuild.services.store.filter.DataViewFilterStore.CLASS_NAME;
import static org.cmdbuild.services.store.filter.DataViewFilterStore.DESCRIPTION;
import static org.cmdbuild.services.store.filter.DataViewFilterStore.FILTER;
import static org.cmdbuild.services.store.filter.DataViewFilterStore.NAME;
import static org.cmdbuild.services.store.filter.DataViewFilterStore.SHARED;
import static org.cmdbuild.services.store.filter.DataViewFilterStore.USER_ID;

import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.dao.BaseStorableConverter;
import org.cmdbuild.services.store.filter.FilterStore.Filter;

public class FilterConverter extends BaseStorableConverter<Filter> {

	private final CMDataView dataView;

	public FilterConverter(final CMDataView dataView) {
		this.dataView = dataView;
	}

	@Override
	public String getClassName() {
		return CLASS_NAME;
	}

	@Override
	public Filter convert(final CMCard card) {
		final Long classId = card.get(CLASS_ID, Long.class);
		final CMClass clazz = dataView.findClass(classId);
		return FilterDTO.newFilter() //
				.withId(card.getId()) //
				.withName(card.get(NAME, String.class)) //
				.withDescription(card.get(DESCRIPTION, String.class)) //
				.withClassName(clazz.getIdentifier().getLocalName()) //
				.withConfiguration(card.get(FILTER, String.class)) //
				.thatIsShared(card.get(SHARED, Boolean.class)) //
				.withUserId(card.get(USER_ID, Integer.class, 0).longValue()) //
				.build();
	}

	@Override
	public Map<String, Object> getValues(final Filter storable) {
		final Map<String, Object> values = newHashMap();
		values.put(NAME, storable.getName());
		values.put(DESCRIPTION, storable.getDescription());
		values.put(CLASS_ID, dataView.findClass(storable.getClassName()).getId());
		values.put(FILTER, storable.getConfiguration());
		values.put(SHARED, storable.isShared());
		values.put(USER_ID, storable.getUserId());
		return values;
	}

}
