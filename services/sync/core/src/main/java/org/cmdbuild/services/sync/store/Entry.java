package org.cmdbuild.services.sync.store;

import java.util.Map;

public interface Entry {

	Type getType();

	Iterable<Map.Entry<String, Object>> getValues();

	Object getValue(String name);

	Key getKey();

}
