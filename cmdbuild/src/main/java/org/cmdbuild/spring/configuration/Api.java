package org.cmdbuild.spring.configuration;

import org.cmdbuild.api.fluent.ExecutorBasedFluentApi;
import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.core.api.fluent.LogicFluentApiExecutor;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Api {

	@Autowired
	private Data data;

	@Autowired
	private LookupLogic lookupLogic;

	@Bean
	public FluentApi systemFluentApi() {
		return new ExecutorBasedFluentApi(systemFluentApiExecutor());
	}

	@Bean
	public FluentApiExecutor systemFluentApiExecutor() {
		return new LogicFluentApiExecutor(data.systemDataAccessLogicBuilder().build(), lookupLogic);
	}

}
