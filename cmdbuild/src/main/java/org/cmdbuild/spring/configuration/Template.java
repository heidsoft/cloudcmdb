package org.cmdbuild.spring.configuration;

import org.cmdbuild.data.store.dao.DataViewStore;
import org.cmdbuild.data.store.dao.StorableConverter;
import org.cmdbuild.services.template.engine.DatabaseEngine;
import org.cmdbuild.services.template.store.StoreTemplateRepository;
import org.cmdbuild.services.template.store.TemplateStorableConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Template {

	@Autowired
	private Data data;

	@Bean
	public DatabaseEngine databaseTemplateEngine() {
		return DatabaseEngine.newInstance() //
				.withTemplateRepository(storeTemplateRepository()) //
				.build();
	}

	@Bean
	public StoreTemplateRepository storeTemplateRepository() {
		return new StoreTemplateRepository(templateDataViewStore());
	}

	@Bean
	protected DataViewStore<org.cmdbuild.services.template.store.Template> templateDataViewStore() {
		return DataViewStore.newInstance(data.systemDataView(), templateStorableConverter());
	}

	@Bean
	protected StorableConverter<org.cmdbuild.services.template.store.Template> templateStorableConverter() {
		return new TemplateStorableConverter();
	}

}
