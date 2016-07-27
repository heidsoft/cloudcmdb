package org.cmdbuild.services.sync.store.internal;

import java.util.Map;

import org.cmdbuild.services.sync.store.ClassType;

public interface AttributeValueAdapter {

	Iterable<Map.Entry<String, Object>> toInternal(ClassType type,
			Iterable<? extends Map.Entry<String, ? extends Object>> values);

	Iterable<Map.Entry<String, Object>> toSynchronizer(ClassType type,
			Iterable<? extends Map.Entry<String, ? extends Object>> values);

}
