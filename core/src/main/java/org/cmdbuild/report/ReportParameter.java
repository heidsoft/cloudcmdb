package org.cmdbuild.report;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.cmdbuild.common.Constants.DATETIME_TWO_DIGIT_YEAR_FORMAT;
import static org.cmdbuild.common.Constants.DATE_FOUR_DIGIT_YEAR_FORMAT;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sf.jasperreports.engine.JRParameter;

import org.cmdbuild.exception.ReportException.ReportExceptionType;

/**
 * 
 * Wrapper for user-defined Jasper Parameter
 * 
 * AVAILABLE FORMATS FOR JRPARAMETER NAME 1) reference: "label.class.attribute"
 * - ie: User.Users.Description 2) lookup: "label.lookup.lookuptype" - ie:
 * Brand.Lookup.Brands 3) simple: "label" - ie: My parameter
 * 
 * Notes: - The description property overrides the label value - Reference or
 * lookup parameters will always be integers while simple parameters will match
 * original parameter class - All custom parameters are required; set a property
 * (in iReport) with name="required" and value="false" to override
 * 
 */
public abstract class ReportParameter {

	/**
	 * @deprecated regular expression matching lookup and reference parameters
	 *             format.
	 */
	@Deprecated
	private static final String LEGACY_NAME_PATTERN = "[\\w\\s]*\\.\\w*\\.[\\w\\s]*";

	// create the right subclass
	public static ReportParameter parseJrParameter(final JRParameter jrParameter) {
		if (jrParameter == null || jrParameter.getName() == null || jrParameter.getName().equals("")) {
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_FORMAT.createException();
		}

		final String iReportParamName = jrParameter.getName();
		final ReportParameter output;
		if (iReportParamName.indexOf(".") == -1) {
			final CustomProperties customProperties = new CustomProperties(jrParameter.getPropertiesMap());
			if (customProperties.hasLookupType()) {
				final String lookupType = customProperties.getLookupType();
				output = new RPLookup(jrParameter, iReportParamName, lookupType);
			} else if (customProperties.hasTargetClass()) {
				final String targetClass = customProperties.getTargetClass();
				output = new RPReference(jrParameter, iReportParamName, targetClass);
			} else {
				output = new RPSimple(jrParameter, iReportParamName);
			}
		} else {
			/*
			 * LEGACY
			 */
			if (!iReportParamName.matches(LEGACY_NAME_PATTERN)) {
				throw ReportExceptionType.REPORT_INVALID_PARAMETER_FORMAT.createException();
			}
			final String[] split = iReportParamName.split("\\.");
			if (split[1].equalsIgnoreCase("lookup")) {
				output = new RPLookup(jrParameter, split[0], split[2]);
			} else {
				output = new RPReference(jrParameter, split[0], split[1]);
			}
		}
		return output;
	}

	private final JRParameter jrParameter;
	private final String name;
	private Object parameterValue;

	/**
	 * Usable by subclasses only.
	 */
	protected ReportParameter(final JRParameter jrParameter, final String name) {
		this.jrParameter = jrParameter;
		this.name = name;
	}

	public abstract void accept(ReportParameterVisitor visitor);

	public String getDefaultValue() {
		if (jrParameter.getDefaultValueExpression() != null) {
			final GroovyShell shell = new GroovyShell();
			final Script sc = shell.parse(jrParameter.getDefaultValueExpression().getText());
			final Object result = sc.run();

			if (result != null) {
				if (jrParameter.getValueClass() == Date.class) {
					final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FOUR_DIGIT_YEAR_FORMAT);
					return sdf.format(result);
				} else if (jrParameter.getValueClass() == Timestamp.class || jrParameter.getValueClass() == Time.class) {
					final SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_TWO_DIGIT_YEAR_FORMAT);
					return sdf.format(result);
				}
				return result.toString();
			}
		}
		return null;
	}

	public boolean hasDefaultValue() {
		return (jrParameter.getDefaultValueExpression() != null
				&& jrParameter.getDefaultValueExpression().getText() != null && !jrParameter
				.getDefaultValueExpression().getText().equals(""));
	}

	public JRParameter getJrParameter() {
		return jrParameter;
	}

	public String getName() {
		return name;
	}

	public String getFullName() {
		return jrParameter.getName();
	}

	public String getDescription() {
		return defaultString(jrParameter.getDescription(), getName());
	}

	public void parseValue(final Object newValue) {
		setValue(newValue);
	}

	public void setValue(final Object parameterValue) {
		this.parameterValue = parameterValue;
	}

	public Object getValue() {
		return parameterValue;
	}

	public boolean isRequired() {
		return new CustomProperties(jrParameter.getPropertiesMap()).isRequired();
	}

}