package org.cmdbuild.data.store;

import java.util.Collection;

import org.cmdbuild.dao.logging.LoggingSupport;
import org.slf4j.Logger;

public interface Store<T extends Storable> {

	Logger logger = LoggingSupport.logger;

	Storable create(T storable);

	T read(Storable storable);

	Collection<T> readAll();

	Collection<T> readAll(Groupable groupable);

	void update(T storable);

	void delete(Storable storable);

}
