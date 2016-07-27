package org.cmdbuild.spring.configuration;

import org.cmdbuild.data.store.Groupable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.StoreFactory;
import org.cmdbuild.data.store.dao.DataViewStore;
import org.cmdbuild.data.store.translation.TranslationConverter;
import org.cmdbuild.logic.translation.DefaultTranslationLogic;
import org.cmdbuild.logic.translation.SetupFacade;
import org.cmdbuild.logic.translation.TranslationFacade;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.services.localization.RequestHandlerSetupFacade;
import org.cmdbuild.servlets.json.serializers.DefaultTranslationFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Translation {

	@Autowired
	private Data data;

	@Autowired
	private Setup setup;

	@Bean
	public TranslationFacade translationFacade() {
		return new DefaultTranslationFacade(translationLogic(), requestHandlerSetupFacade());
	}

	public static final String REQUEST_HANDLER_SETUP_FACADE = "RequestHandlerSetupFacade";

	@Bean(name = REQUEST_HANDLER_SETUP_FACADE)
	public RequestHandlerSetupFacade requestHandlerSetupFacade() {
		return new RequestHandlerSetupFacade(setup.setupFacade());
	}

	@Bean
	public TranslationLogic translationLogic() {
		return new DefaultTranslationLogic(translationStoreFactory(), setup.setupFacade());
	}

	@Bean
	protected StoreFactory<org.cmdbuild.data.store.translation.Translation> translationStoreFactory() {
		return new StoreFactory<org.cmdbuild.data.store.translation.Translation>() {

			@Override
			public Store<org.cmdbuild.data.store.translation.Translation> create(final Groupable groupable) {
				return DataViewStore.newInstance(data.systemDataView(), groupable, translationConverter());
			}

		};
	}

	@Bean
	protected TranslationConverter translationConverter() {
		return new TranslationConverter();
	}

}
