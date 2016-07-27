package org.cmdbuild.services.sync.store;

public interface Store {

	void create(Entry entry);

	Iterable<Entry> readAll();

	void update(Entry entry);

	void delete(Entry entry);

}
