package org.cmdbuild.report;

import static com.google.common.collect.Maps.newHashMap;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trim;

import java.util.Map;

import net.sf.jasperreports.engine.JRPropertiesMap;

public class CustomProperties {

	private static final String //
			FILTER = "filter", //
			FILTER_PREFIX = FILTER + ".", //
			REQUIRED = "required", //
			LOOKUP_TYPE = "lookupType", //
			TARGET_CLASS = "targetClass" //
			;

	private final JRPropertiesMap delegate;

	public CustomProperties(final JRPropertiesMap delegate) {
		this.delegate = delegate;
	}

	public boolean isRequired() {
		return toBoolean(get(REQUIRED));
	}

	public String getFilter() {
		return get(FILTER);
	}

	public Map<String, String> getFilterParameters() {
		final Map<String, String> output = newHashMap();
		for (final String name : delegate.getPropertyNames()) {
			if (name.startsWith(FILTER_PREFIX)) {
				output.put(trim(name.substring(FILTER_PREFIX.length())), get(name));
			}
		}
		return output;
	}

	public boolean hasLookupType() {
		return !isBlank(get(LOOKUP_TYPE));
	}

	public String getLookupType() {
		return get(LOOKUP_TYPE);
	}

	public boolean hasTargetClass() {
		return !isBlank(get(TARGET_CLASS));
	}

	public String getTargetClass() {
		return get(TARGET_CLASS);
	}

	private String get(final String name) {
		return trim(delegate.getProperty(name));
	}

}
