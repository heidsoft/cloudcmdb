package org.cmdbuild.logic.filter;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.logic.Logic;

public interface FilterLogic extends Logic {

	interface Filter {

		Long getId();

		String getName();

		String getDescription();

		String getClassName();

		String getConfiguration();

		boolean isShared();

	}

	Filter create(Filter filter);

	void update(Filter filter);

	void delete(Filter filter);

	PagedElements<Filter> readForCurrentUser(String className);

	PagedElements<Filter> readShared(String className, int start, int limit);

	PagedElements<Filter> readNotShared(String className, int start, int limit);

	Iterable<Filter> getDefaults(String className, String groupName);

	void setDefault(Iterable<Long> filters, Iterable<String> groups);

	Iterable<String> getGroups(Long filter);

}
