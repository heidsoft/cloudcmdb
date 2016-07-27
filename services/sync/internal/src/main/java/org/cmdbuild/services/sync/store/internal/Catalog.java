package org.cmdbuild.services.sync.store.internal;

import org.cmdbuild.services.sync.store.Type;

public interface Catalog {

	Iterable<Type> getTypes();

	<T extends Type> T getType(String name, Class<T> type);

}
