package org.cmdbuild.service.rest.v2.cxf.configuration;

import org.cmdbuild.service.rest.v2.cxf.localization.LocalizationHandler;
import org.cmdbuild.service.rest.v2.cxf.util.Messages.HeaderValue;
import org.cmdbuild.service.rest.v2.logging.LoggingSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MiscV2 implements LoggingSupport {

	private static final String LOCALIZED_KEY = "CMDBuild-Localized";
	private static final String LOCALIZATION_KEY = "CMDBuild-Localization";

	@Autowired
	private ApplicationContextHelperV2 helper;

	@Autowired
	private ServicesV2 services;

	@Bean
	public LocalizationHandler v2_localizationHandler() {
		return new LocalizationHandler(v2_localizedFromMessage(), v2_localizationFromMessage(), helper.requestHandler());
	}

	@Bean
	protected HeaderValue v2_localizedFromMessage() {
		return HeaderValue.of(LOCALIZED_KEY);
	}

	@Bean
	protected HeaderValue v2_localizationFromMessage() {
		return HeaderValue.of(LOCALIZATION_KEY);
	}

}
