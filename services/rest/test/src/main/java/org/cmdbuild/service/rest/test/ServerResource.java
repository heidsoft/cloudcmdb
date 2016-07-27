package org.cmdbuild.service.rest.test;

import static com.google.common.reflect.Reflection.newProxy;
import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.Range.between;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import org.apache.commons.lang3.Range;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerResource<T> extends ExternalResource {

	private static final Logger logger = LoggerFactory.getLogger(ServerResource.class);

	public static class Builder<T> implements org.apache.commons.lang3.builder.Builder<ServerResource<T>> {

		private static final int DEFAULT_RETRIES = 3;

		private final Class<T> serviceClass;
		private T service;
		private Range<Integer> portRange;
		private Integer retries;

		public Builder(final Class<T> serviceClass) {
			this.serviceClass = serviceClass;
		}

		@Override
		public ServerResource<T> build() {
			validate();
			return new ServerResource<T>(this);
		}

		private void validate() {
			retries = defaultIfNull(retries, DEFAULT_RETRIES);
		}

		public Builder<T> withService(final T service) {
			this.service = service;
			return this;
		}

		public Builder<T> withPortRange(final Range<Integer> range) {
			this.portRange = range;
			return this;
		}

		public Builder<T> setRetries(final int retries) {
			this.retries = retries;
			return this;
		}

	}

	public static <T> Builder<T> newInstance(final Class<T> serviceClass) {
		return new Builder<>(serviceClass);
	}

	public static final int DEFAULT_RANGE_MIN_INCLUSIVE = 1024;
	public static final int DEFAULT_RANGE_MAX_INCLUSIVE = 65535;
	public static final Range<Integer> DEFAULT_RANGE = between(DEFAULT_RANGE_MIN_INCLUSIVE,
			DEFAULT_RANGE_MAX_INCLUSIVE);

	public static Range<Integer> randomPort() {
		return randomPort(DEFAULT_RANGE);
	}

	public static Range<Integer> randomPort(final Range<Integer> range) {
		return range;
	}

	private static class DelegatingService<T> {

		private final T proxy;
		private T delegate;

		private DelegatingService(final Class<T> type) {
			proxy = newProxy(type, new InvocationHandler() {

				@Override
				public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
					try {
						return method.invoke(delegate, args);
					} catch (final InvocationTargetException e) {
						throw e.getCause();
					}
				}

			});
		}

		public T delegate() {
			return proxy;
		}

		public void delegate(final T delegate) {
			this.delegate = delegate;
		}

	}

	private static final Random random = new Random();

	public static final JacksonJaxbJsonProvider JSON_PROVIDER = new JacksonJaxbJsonProvider();

	private final Class<T> serviceClass;
	private final DelegatingService<T> service;
	private final Range<Integer> portRange;
	private final int retries;

	private String address;
	private Server server;

	private ServerResource(final Builder<T> builder) {
		this.serviceClass = builder.serviceClass;
		this.service = new DelegatingService<T>(serviceClass) {
			{
				delegate(builder.service);
			}
		};
		this.portRange = builder.portRange;
		this.retries = builder.retries;
	}

	@Override
	protected void before() throws Throwable {
		super.before();
		boolean started = false;
		for (int count = 0; (count < retries) && !started; count++) {
			try {
				logger.info("server starting...");
				final Integer _port = random.nextInt(portRange.getMaximum() - portRange.getMinimum())
						+ portRange.getMinimum();
				address = format("http://localhost:%d", _port);
				final JAXRSServerFactoryBean serverFactory = new JAXRSServerFactoryBean();
				serverFactory.setResourceClasses(serviceClass);
				serverFactory.setResourceProvider(serviceClass, new SingletonResourceProvider(service.delegate()));
				serverFactory.setAddress(address);
				serverFactory.setProvider(JSON_PROVIDER);

				server = serverFactory.create();
				logger.info(format("server ready at ", address));
				started = true;
			} catch (final Exception e) {
				logger.warn("error starting server", e);
			}
		}
		if (!started) {
			throw new RuntimeException("server cannot be started");
		}
	}

	@Override
	protected void after() {
		logger.info("server exiting...");
		server.destroy();
	}

	public void service(final T service) {
		this.service.delegate(service);
	}

	public String resource(final String resource) {
		return format("%s/%s", address, resource);
	}

}
