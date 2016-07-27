package org.cmdbuild.listeners;

public interface ValuesStore {

	Object get(String name);

	void set(String name, Object value);

	void remove(String name);

}
