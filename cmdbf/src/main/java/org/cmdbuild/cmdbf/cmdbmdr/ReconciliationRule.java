package org.cmdbuild.cmdbf.cmdbmdr;

import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.json.JSONObject;

@XmlRootElement(name = "Rule")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReconciliationRule {
	static private final String MDRID_KEY = "mdrId";
	static private final String REGEX_KEY = "regex";
	static private final String QUERY_KEY = "query";
	static private final String SOURCE_KEY = "source";
	static private final String TARGET_KEY = "target";
	static private final String TYPE_KEY = "type";;
	static private final String SOURCETYPE_KEY = "sourceType";;
	static private final String TARGETTYPE_KEY = "targetType";
	static private final String FILTER_KEY = "filter";
	static private final String FORMAT_KEY = "format";

	private String mdrId;
	private String regex;
	private String query;
	private String source;
	private String target;
	private String type;
	private String sourceType;
	private String targetType;
	private String filter;
	private String format;

	@XmlTransient
	private Pattern regexPattern;

	public static ReconciliationRule parse(final String value) throws Exception {
		final ReconciliationRule rule = new ReconciliationRule();

		final JSONObject json = new JSONObject(value);
		if (json.has(MDRID_KEY)) {
			rule.setMdrId(json.getString(MDRID_KEY));
		}
		if (json.has(REGEX_KEY)) {
			rule.setRegex(json.getString(REGEX_KEY));
		}
		if (json.has(QUERY_KEY)) {
			rule.setQuery(json.getString(QUERY_KEY));
		}
		if (json.has(SOURCE_KEY)) {
			rule.setSource(json.getString(SOURCE_KEY));
		}
		if (json.has(TARGET_KEY)) {
			rule.setTarget(json.getString(TARGET_KEY));
		}
		if (json.has(TYPE_KEY)) {
			rule.setType(json.getString(TYPE_KEY));
		}
		if (json.has(SOURCETYPE_KEY)) {
			rule.setSourceType(json.getString(SOURCETYPE_KEY));
		}
		if (json.has(TARGETTYPE_KEY)) {
			rule.setTargetType(json.getString(TARGETTYPE_KEY));
		}
		if (json.has(FILTER_KEY)) {
			rule.setFilter(json.getString(FILTER_KEY));
		}
		if (json.has(FORMAT_KEY)) {
			rule.setFormat(json.getString(FORMAT_KEY));
		}
		return rule;
	}

	public void setMdrId(final String mdrId) {
		this.mdrId = mdrId;
	}

	public String getMdrId() {
		return mdrId;
	}

	public void setRegex(final String regex) {
		this.regex = regex;
		this.regexPattern = null;
	}

	public Pattern getRegex() {
		if (regexPattern == null && regex != null) {
			regexPattern = Pattern.compile(regex);
		}
		return regexPattern;
	}

	public void setQuery(final String query) {
		this.query = query;
	}

	public String getQuery() throws Exception {
		return query;
	}

	public void setSource(final String source) {
		this.source = source;
	}

	public String getSource() throws Exception {
		return source;
	}

	public void setTarget(final String target) {
		this.target = target;
	}

	public String getTarget() throws Exception {
		return target;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setSourceType(final String sourceType) {
		this.sourceType = sourceType;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setTargetType(final String targetType) {
		this.targetType = targetType;
	}

	public String getTargetType() {
		return targetType;
	}

	public void setFilter(final String filter) {
		this.filter = filter;
	}

	public String getFilter() {
		return filter;
	}

	public void setFormat(final String format) {
		this.format = format;
	}

	public String getFormat() {
		return format;
	}
}
