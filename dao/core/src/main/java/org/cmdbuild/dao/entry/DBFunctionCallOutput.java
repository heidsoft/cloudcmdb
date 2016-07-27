package org.cmdbuild.dao.entry;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.HashMap;
import java.util.Map;

public class DBFunctionCallOutput implements CMValueSet {

	private final Map<String, Object> values;

	public DBFunctionCallOutput() {
		this.values = new HashMap<String, Object>();
	}

	@Override
	public final Object get(final String key) {
		return values.get(key);
	}

	@Override
	public <T> T get(final String key, final Class<? extends T> requiredType) {
		final Object value = get(key);
		return requiredType.cast(value);
	}

	@Override
	public <T> T get(final String key, final Class<? extends T> requiredType, final T defaultValue) {
		return defaultIfNull(get(key, requiredType), defaultValue);
	}

	@Override
	public Iterable<Map.Entry<String, Object>> getValues() {
		return values.entrySet();
	}

	public final DBFunctionCallOutput set(final String key, final Object value) {
		values.put(key, value);
		return this;
	}

}
