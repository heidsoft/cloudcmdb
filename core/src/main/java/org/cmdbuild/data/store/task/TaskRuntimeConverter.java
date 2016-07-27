package org.cmdbuild.data.store.task;

import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.data.store.dao.BaseStorableConverter;
import org.joda.time.DateTime;

import com.google.common.collect.Maps;

public class TaskRuntimeConverter extends BaseStorableConverter<TaskRuntime> {

	private static final String CLASSNAME = "_TaskRuntime";

	private static final String OWNER = "Owner";
	private static final String LAST_EXECUTION = "LastExecution";

	@Override
	public String getClassName() {
		return CLASSNAME;
	}

	@Override
	public TaskRuntime convert(final CMCard card) {
		return TaskRuntime.newInstance() //
				.withId(card.getId()) //
				.withOwner(card.get(OWNER, IdAndDescription.class).getId()) //
				.withLastExecution(card.get(LAST_EXECUTION, DateTime.class)) //
				.build();
	}

	@Override
	public Map<String, Object> getValues(final TaskRuntime storable) {
		final Map<String, Object> values = Maps.newHashMap();
		values.put(OWNER, storable.getOwner());
		values.put(LAST_EXECUTION, storable.getLastExecution());
		return values;
	}

}
