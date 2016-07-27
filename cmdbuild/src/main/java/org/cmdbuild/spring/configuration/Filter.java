package org.cmdbuild.spring.configuration;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.data.store.dao.StorableConverter;
import org.cmdbuild.logic.filter.DefaultFilterLogic;
import org.cmdbuild.logic.filter.DefaultFilterLogic.Converter;
import org.cmdbuild.logic.filter.FilterLogic;
import org.cmdbuild.services.localization.LocalizedStorableConverter;
import org.cmdbuild.services.store.filter.DataViewFilterStore;
import org.cmdbuild.services.store.filter.FilterConverter;
import org.cmdbuild.services.store.filter.FilterStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Filter {

	@Autowired
	private Data data;

	@Autowired
	private Report report;

	@Autowired
	private Translation translation;

	@Autowired
	private UserStore userStore;

	@Bean
	public FilterLogic defaultFilterLogic() {
		return new DefaultFilterLogic(dataViewFilterStore(), defaultFilterLogicConverter(), userStore);
	}

	@Bean
	protected FilterStore dataViewFilterStore() {
		return new DataViewFilterStore(data.systemDataView(), localizedStorableConverter());
	}

	@Bean
	protected StorableConverter<FilterStore.Filter> localizedStorableConverter() {
		return new LocalizedStorableConverter<FilterStore.Filter>(baseStorableConverter(),
				translation.translationFacade(), data.systemDataView(), report.reportLogic());
	}

	@Bean
	protected StorableConverter<FilterStore.Filter> baseStorableConverter() {
		return new FilterConverter(data.systemDataView());
	}

	@Bean
	protected Converter defaultFilterLogicConverter() {
		return new DefaultFilterLogic.DefaultConverter(filterConverter());
	}

	@Bean
	protected com.google.common.base.Converter<FilterLogic.Filter, FilterStore.Filter> filterConverter() {
		return new DefaultFilterLogic.FilterConverter(userStore);
	}

}
