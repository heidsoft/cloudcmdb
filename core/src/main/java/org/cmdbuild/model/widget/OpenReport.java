package org.cmdbuild.model.widget;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OpenReport extends Widget {

	private static List<String> NO_ATTRIBUTES = Collections.emptyList();

	private String reportCode;
	private String forceFormat;
	private Map<String, Object> preset;
	private List<String> readOnlyAttributes;

	@Override
	public void accept(final WidgetVisitor visitor) {
		visitor.visit(this);
	}

	public void setReportCode(final String reportCode) {
		this.reportCode = reportCode;
	}

	public String getReportCode() {
		return reportCode;
	}

	public void setForceFormat(final String forceFormat) {
		this.forceFormat = forceFormat;
	}

	public String getForceFormat() {
		return forceFormat;
	}

	public void setPreset(final Map<String, Object> preset) {
		this.preset = preset;
	}

	public Map<String, Object> getPreset() {
		return preset;
	}

	public List<String> getReadOnlyAttributes() {
		return (readOnlyAttributes == null) ? NO_ATTRIBUTES : readOnlyAttributes;
	}

	public void setReadOnlyAttributes(final List<String> readOnlyAttributes) {
		this.readOnlyAttributes = defaultIfNull(readOnlyAttributes, NO_ATTRIBUTES);
	}

}
