package org.cmdbuild.webapp;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import org.cmdbuild.config.GisProperties;

public class Management {

	public boolean isGisEnabled() {
		return gisProperties().isEnabled();
	}

	public boolean isGoogleServiceOn() {
		return gisProperties().isServiceOn(GisProperties.GOOGLE);
	}

	public boolean isYahooServiceOn() {
		return gisProperties().isServiceOn(GisProperties.YAHOO);
	}

	public String getYahooKey() {
		return gisProperties().getYahooKey();
	}

	private static GisProperties gisProperties() {
		return applicationContext().getBean(GisProperties.class);
	}

}