package org.cmdbuild.dao.entry;

import java.util.Map.Entry;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingValueSet extends ForwardingObject implements CMValueSet {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingValueSet() {
	}

	@Override
	protected abstract CMValueSet delegate();

	@Override
	public Object get(final String key) {
		return delegate().get(key);
	}

	@Override
	public <T> T get(final String key, final Class<? extends T> requiredType) {
		return delegate().get(key, requiredType);
	}

	@Override
	public <T> T get(final String key, final Class<? extends T> requiredType, final T defaultValue) {
		return delegate().get(key, requiredType, defaultValue);
	}

	@Override
	public Iterable<Entry<String, Object>> getValues() {
		return delegate().getValues();
	}

}
