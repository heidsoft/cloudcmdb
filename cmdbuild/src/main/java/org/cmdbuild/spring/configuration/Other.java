package org.cmdbuild.spring.configuration;

import org.cmdbuild.common.java.sql.DataSourceHelper;
import org.cmdbuild.common.java.sql.DefaultDataSourceHelper;
import org.cmdbuild.services.meta.DefaultMetadataStoreFactory;
import org.cmdbuild.services.meta.MetadataStoreFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Other {

	@Autowired
	private Data data;

	@Bean
	public MetadataStoreFactory metadataStoreFactory() {
		return new DefaultMetadataStoreFactory(data.systemDataView());
	}

	@Bean
	public DataSourceHelper dataSourceHelper() {
		return new DefaultDataSourceHelper();
	}

}
