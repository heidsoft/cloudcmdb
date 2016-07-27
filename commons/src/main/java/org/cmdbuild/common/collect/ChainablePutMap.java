package org.cmdbuild.common.collect;

import java.util.Map;

import com.google.common.collect.ForwardingMap;

public final class ChainablePutMap<K, V> extends ForwardingMap<K, V> {

	public static <K, V> ChainablePutMap<K, V> of(final Map<K, V> delegate) {
		return new ChainablePutMap<K, V>(delegate);
	}

	private final Map<K, V> delegate;

	private ChainablePutMap(final Map<K, V> delegate) {
		this.delegate = delegate;
	}

	@Override
	protected Map<K, V> delegate() {
		return delegate;
	}

	public ChainablePutMap<K, V> chainablePut(final K key, final V value) {
		super.put(key, value);
		return this;
	}

	public ChainablePutMap<K, V> chainablePutAll(final Map<? extends K, ? extends V> map) {
		super.putAll(map);
		return this;
	}

}
