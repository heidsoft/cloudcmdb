package org.cmdbuild.data.store.lookup;

import org.cmdbuild.data.store.Store;

public interface LookupStore extends Store<Lookup> {

	Iterable<Lookup> readAll(LookupType type);

	Iterable<LookupType> readAllTypes();
	
	Iterable<Lookup> readFromUuid(String uuid);

}
