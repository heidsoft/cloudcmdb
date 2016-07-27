package org.cmdbuild.dms.alfresco;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.exception.MissingConfigurationException;

public interface AlfrescoDmsConfiguration extends DmsConfiguration {

	String getServerURL();

	String getFtpPort();

	String getFtpHost();

	String getAlfrescoUser();

	String getAlfrescoPassword();

	String getRepositoryFSPath();

	String getRepositoryWSPath();

	String getRepositoryApp();

	String getAlfrescoCustomUri();

	String getAlfrescoCustomPrefix();

	long getDelayBetweenFtpAndWebserviceOperations();

	/*
	 * Utilities
	 */

	class NullDmsConfiguration {

		private NullDmsConfiguration() {
			// prevents instantiation
		}

		private static Object createProxy() {
			final Class<?> cl = DmsConfiguration.class;
			final ClassLoader classLoader = cl.getClassLoader();
			final Class<?>[] interfaces = new Class<?>[] { DmsConfiguration.class };
			return Proxy.newProxyInstance( //
					classLoader, //
					interfaces, //
					new InvocationHandler() {
						@Override
						public Object invoke(final Object proxy, final Method method, final Object[] args)
								throws Throwable {
							throw new MissingConfigurationException();
						}
					});
		}

		public static DmsConfiguration newInstance() {
			final Object proxy = createProxy();
			return DmsConfiguration.class.cast(proxy);
		}

	}

}
