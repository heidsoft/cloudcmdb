package org.cmdbuild.service.rest.v2.cxf.util;

import java.util.List;

import com.google.common.collect.Maps.EntryTransformer;

public class Maps {

	private static class FirstElement<K, V> implements EntryTransformer<K, List<? extends V>, V> {

		@Override
		public V transformEntry(final K key, final List<? extends V> value) {
			return value.isEmpty() ? null : value.get(0);
		}

	};

	public static <K, V> EntryTransformer<K, List<? extends V>, V> firstElement() {
		return new FirstElement<K, V>();
	}

	private Maps() {
		// prevents instantiation
	}

}
