package org.cmdbuild.services.event;

import static org.apache.commons.lang3.StringUtils.defaultString;

import java.io.Reader;
import java.io.StringReader;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.logic.taskmanager.TaskManagerLogic;
import org.cmdbuild.services.event.Contexts.AfterCreate;
import org.cmdbuild.services.event.Contexts.AfterUpdate;
import org.cmdbuild.services.event.Contexts.BeforeDelete;
import org.cmdbuild.services.event.Contexts.BeforeUpdate;
import org.cmdbuild.spring.SpringIntegrationUtils;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class ScriptCommand implements Command {

	private static final Marker marker = MarkerFactory.getMarker(ScriptCommand.class.getName());

	private static final String CURRENT = "__current__";
	private static final String LOGGER = "__logger__";
	private static final String NEXT = "__next__";
	private static final String PREVIOUS = "__previous__";

	private static final String CMDB = "__cmdb__";
	private static final String CMDB_V1 = "__cmdb_v1__";

	public interface ScriptLogging {

		public void info(String msg);

		public void info(String format, Object... arguments);

		public void error(String msg);

		public void error(String format, Object[] arguments);

		public void warn(String msg);

		public void warn(String format, Object[] arguments);

	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ScriptCommand> {

		private String engine;
		private String script;
		private FluentApi fluentApi;

		private Builder() {
			// use factory method
		}

		@Override
		public ScriptCommand build() {
			validate();
			return new ScriptCommand(this);
		}

		private void validate() {
			Validate.notBlank(engine, "invalid engine");
			script = defaultString(script);
			Validate.notNull(fluentApi, "invalid api");
		}

		public Builder withEngine(final String engine) {
			this.engine = engine;
			return this;
		}

		public Builder withScript(final String script) {
			this.script = script;
			return this;
		}

		public Builder withFluentApi(final FluentApi fluentApi) {
			this.fluentApi = fluentApi;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final String engine;
	private final String script;
	private final FluentApi fluentApi;

	private ScriptCommand(final Builder builder) {
		this.engine = builder.engine;
		this.script = builder.script;
		this.fluentApi = builder.fluentApi;
	}

	@Override
	public void execute(final Context context) {
		try {
			final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
			final Bindings bindings = scriptEngineManager.getBindings();
			fillBindings(bindings, context);
			final ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(engine);
			final Reader reader = new StringReader(script);
			scriptEngine.eval(reader, bindings);
		} catch (final Exception e) {
			logger.warn(marker, "cannot execute script", e);
			throw new RuntimeException(e);
		}
	}

	private void fillBindings(final Bindings bindings, final Context context) {
		// <<<<<<<<<<<<<<<<<<<
		bindings.put("__taskmanagerlogic__",
				SpringIntegrationUtils.applicationContext().getBean(TaskManagerLogic.class));
		// <<<<<<<<<<<<<<<<<<<

		bindings.put(CMDB, fluentApi);
		bindings.put(CMDB_V1, fluentApi);

		final ScriptLogging logging = new ScriptLogging() {
			private final Logger logger = Command.logger;

			private final String loggerPrefix = String.format("[script %s] ", context.getClass().getSimpleName());

			@Override
			public void info(final String msg) {
				logger.info("{} {}", loggerPrefix, msg);
			}

			@Override
			public void info(final String format, final Object... arguments) {
				logger.info(loggerPrefix + format, arguments);
			}

			@Override
			public void error(final String msg) {
				logger.warn("{} {}", loggerPrefix, msg);
			}

			@Override
			public void error(final String format, final Object[] arguments) {
				logger.error(loggerPrefix + format, arguments);
			}

			@Override
			public void warn(final String msg) {
				logger.warn("{} {}", loggerPrefix, msg);
			}

			@Override
			public void warn(final String format, final Object[] arguments) {
				logger.warn(loggerPrefix + format, arguments);
			}

		};

		context.accept(new ContextVisitor() {
			@Override
			public void visit(final AfterCreate context) {
				bindings.put(CURRENT, context.card);
				bindings.put(LOGGER, logging);
			}

			@Override
			public void visit(final BeforeUpdate context) {
				bindings.put(CURRENT, context.actual);
				bindings.put(NEXT, context.next);
				bindings.put(LOGGER, logging);
			}

			@Override
			public void visit(final AfterUpdate context) {
				bindings.put(PREVIOUS, context.previous);
				bindings.put(CURRENT, context.actual);
				bindings.put(LOGGER, logging);
			}

			@Override
			public void visit(final BeforeDelete context) {
				bindings.put(CURRENT, context.card);
				bindings.put(LOGGER, logging);
			}
		});

	}

}
