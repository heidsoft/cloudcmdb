package org.cmdbuild.common.template.engine;

import static java.util.Arrays.asList;
import static java.util.regex.Matcher.quoteReplacement;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.common.template.engine.RegexUtils.capture;
import static org.cmdbuild.common.template.engine.RegexUtils.exclude;
import static org.cmdbuild.common.template.engine.RegexUtils.group;
import static org.cmdbuild.common.template.engine.RegexUtils.oneOrMore;
import static org.cmdbuild.common.template.engine.RegexUtils.wordCharacter;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cmdbuild.common.logging.LoggingSupport;
import org.cmdbuild.common.template.TemplateResolver;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

/**
 * {@link TemplateResolver} based on {@link Engine}s.
 */
public class EngineBasedTemplateResolver implements TemplateResolver, LoggingSupport {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<EngineBasedTemplateResolver> {

		private final Map<String, Engine> engines = Maps.newHashMap();

		private Builder() {
			// use factory method
		}

		public Builder withEngine(final Engine engine, final String... prefixes) {
			return withEngine(engine, asList(prefixes));
		}

		public Builder withEngine(final Engine engine, final Iterable<String> prefixes) {
			for (final String p : prefixes) {
				engines.put(p, engine);
			}
			return this;
		}

		// TODO add predicate

		@Override
		public EngineBasedTemplateResolver build() {
			return new EngineBasedTemplateResolver(this);
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private static final String START_TEMPLATE = "\\{";
	private static final String END_TEMPLATE = "\\}";
	private static final String KEY_VALUE_SEPARATOR = ":";
	private static final String ENGINE_PREFIX = capture(oneOrMore(wordCharacter()));
	private static final String EXPRESSION = capture(oneOrMore(group(exclude(START_TEMPLATE, END_TEMPLATE))));
	private static final String REGEX = Joiner.on(EMPTY) //
			.join(START_TEMPLATE, ENGINE_PREFIX, KEY_VALUE_SEPARATOR, EXPRESSION, END_TEMPLATE);

	private static final Pattern VAR_PATTERN = Pattern.compile(REGEX);

	private final Map<String, Engine> engines;

	private EngineBasedTemplateResolver(final Builder builder) {
		logger.debug("using pattern '{}'", VAR_PATTERN.pattern());
		this.engines = builder.engines;
	}

	@Override
	public String resolve(final String expression) {
		return (expression == null) ? null : resolve0(expression);
	}

	private String resolve0(final String expression) {
		logger.debug("trying to resolve '{}'", expression);
		String resolved = expression;
		Matcher matcher = VAR_PATTERN.matcher(resolved);
		while (matcher.find()) {
			logger.debug("match found");
			final String engine = matcher.group(1);
			final String expressionForEngine = matcher.group(2);
			final Object value = eval(engine, expressionForEngine);
			logger.debug("replacing match with '{}'", value);
			resolved = matcher.replaceFirst(quoteReplacement(String.valueOf(value)));
			matcher = VAR_PATTERN.matcher(resolved);
		}
		return resolved;
	}

	private Object eval(final String enginePrefix, final String expression) {
		logger.debug("evaluating '{}' with engine '{}'", expression, enginePrefix);
		final Engine engine = engines.get(enginePrefix);
		if (engine != null) {
			return engine.eval(expression);
		} else {
			logger.warn("engine with prefix '{}' not found", enginePrefix);
			return null;
		}
	}

}
