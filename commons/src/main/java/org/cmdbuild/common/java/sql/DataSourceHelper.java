package org.cmdbuild.common.java.sql;

import javax.sql.DataSource;

import org.cmdbuild.common.java.sql.DataSourceTypes.DataSourceType;

public interface DataSourceHelper {

	interface Configuration {

		DataSourceType getType();

		String getHost();

		int getPort();

		String getDatabase();

		String getInstance();

		String getUsername();

		String getPassword();

	}

	Iterable<DataSourceType> getAvailableTypes();

	DataSource create(Configuration configuration);

}
