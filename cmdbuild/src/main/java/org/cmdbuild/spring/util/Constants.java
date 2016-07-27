package org.cmdbuild.spring.util;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;

public class Constants {

	public static final String DEFAULT = "default";
	/**
	 * @deprecated use {@link ConfigurableBeanFactory.SCOPE_PROTOTYPE} instead.
	 */
	public static final String PROTOTYPE = ConfigurableBeanFactory.SCOPE_PROTOTYPE;
	public static final String SOAP = "soap";
	public static final String SYSTEM = "system";
	public static final String USER = "user";

	private Constants() {
		// prevents instantiation
	}

}
