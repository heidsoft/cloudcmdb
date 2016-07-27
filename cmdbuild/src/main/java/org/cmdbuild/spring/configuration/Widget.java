package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.PROTOTYPE;

import org.cmdbuild.data.converter.WidgetConverter;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.dao.DataViewStore;
import org.cmdbuild.data.store.dao.StorableConverter;
import org.cmdbuild.logic.widget.WidgetLogic;
import org.cmdbuild.services.localization.LocalizedStorableConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class Widget {

	@Autowired
	private Data data;

	@Autowired
	private Translation translation;

	@Autowired
	private Report report;

	@Bean
	@Scope(PROTOTYPE)
	public WidgetLogic widgetLogic() {
		return new WidgetLogic(widgetStore());
	}

	@Bean
	protected StorableConverter<org.cmdbuild.model.widget.Widget> converter() {
		return new LocalizedStorableConverter<org.cmdbuild.model.widget.Widget>(baseConverter(),
				translation.translationFacade(), data.systemDataView(), report.reportLogic());
	}

	@Bean
	protected Store<org.cmdbuild.model.widget.Widget> widgetStore() {
		return DataViewStore.<org.cmdbuild.model.widget.Widget> newInstance() //
				.withDataView(data.systemDataView()) //
				.withStorableConverter(converter()) //
				.build();
	}

	@Bean
	protected StorableConverter<org.cmdbuild.model.widget.Widget> baseConverter() {
		return new WidgetConverter();
	}

}
