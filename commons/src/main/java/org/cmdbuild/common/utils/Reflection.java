package org.cmdbuild.common.utils;

import static com.google.common.base.Defaults.defaultValue;
import static com.google.common.reflect.Invokable.from;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.defaultString;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.cmdbuild.common.logging.LoggingSupport;

import com.google.common.reflect.AbstractInvocationHandler;

public class Reflection implements LoggingSupport {

	private static class DefaultValues extends AbstractInvocationHandler {

		@Override
		protected Object handleInvocation(final Object proxy, final Method method, final Object[] args)
				throws Throwable {
			return defaultValue(from(method).getReturnType().getRawType());
		}

	}

	private static class UnsupportedInvocationHandler extends AbstractInvocationHandler {

		private static final String UNSUPPORTED = "unsupported";

		private final String message;

		public UnsupportedInvocationHandler(final String message) {
			this.message = message;
		}

		@Override
		protected Object handleInvocation(final Object proxy, final Method method, final Object[] args)
				throws Throwable {
			throw new UnsupportedOperationException(format("%s (%s)", defaultString(message, UNSUPPORTED), method));
		}

	}

	public static InvocationHandler defaultValues() {
		return new DefaultValues();
	}

	public static InvocationHandler unsupported(final String message) {
		return new UnsupportedInvocationHandler(message);
	}

	private Reflection() {
		// prevents instantiation
	}

}
