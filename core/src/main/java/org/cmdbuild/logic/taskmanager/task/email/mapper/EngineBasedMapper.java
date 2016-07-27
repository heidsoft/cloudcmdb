package org.cmdbuild.logic.taskmanager.task.email.mapper;

import static java.lang.String.format;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;
import static java.util.regex.Pattern.quote;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Validate;

import com.google.common.collect.Maps;

public class EngineBasedMapper implements Mapper, MapperEngineVisitor {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<EngineBasedMapper> {

		private String text;
		private MapperEngine engine;

		private Builder() {
			// use factory method
		}

		@Override
		public EngineBasedMapper build() {
			validate();
			return new EngineBasedMapper(this);
		}

		private void validate() {
			Validate.notNull(text, "missing text");
			Validate.notNull(engine, "missing engine");
		}

		public Builder withText(final String text) {
			this.text = text;
			return this;
		}

		public Builder withEngine(final MapperEngine engine) {
			this.engine = engine;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private static final String PATTERN_TEMPLATE = "%s(.*?)%s";

	private final String text;
	private final MapperEngine engine;
	private final Map<String, String> map;

	private EngineBasedMapper(final Builder builder) {
		this.text = builder.text;
		this.engine = builder.engine;
		this.map = Maps.newHashMap();
	}

	@Override
	public Map<String, String> map() {
		engine.accept(this);
		return map;
	}

	@Override
	public void visit(final KeyValueMapperEngine mapper) {
		String resolved = text;
		final Pattern KEY_PATTERN = compile(
				format(PATTERN_TEMPLATE, quote(mapper.getKeyInit()), quote(mapper.getKeyEnd())), DOTALL);
		final Pattern VALUE_PATTERN = compile(
				format(PATTERN_TEMPLATE, quote(mapper.getValueInit()), quote(mapper.getValueEnd())), DOTALL);
		boolean found = true;
		while (found) {
			final Matcher keyMatcher = KEY_PATTERN.matcher(resolved);
			found = keyMatcher.find();
			if (!found) {
				continue;
			}
			final String key = keyMatcher.group(1);
			resolved = resolved.substring(keyMatcher.end());

			/*
			 * Checks for another key before the value. This can happen if the
			 * value is wrongly missing between them.
			 */
			final Matcher secondKeyMatcher = KEY_PATTERN.matcher(resolved);
			final boolean anotherKeyFound = secondKeyMatcher.find();

			final Matcher valueMatcher = VALUE_PATTERN.matcher(resolved);
			found = valueMatcher.find();
			if (!found) {
				continue;
			}
			if (anotherKeyFound && (secondKeyMatcher.start() < valueMatcher.start())) {
				/*
				 * we have two keys without a value between them, skips the
				 * first one
				 */
				continue;
			}
			final String value = valueMatcher.group(1);
			resolved = resolved.substring(valueMatcher.end());

			map.put(key, value);
		}
	}

	@Override
	public void visit(final NullMapperEngine mapper) {
		// nothing to do
	}

}
