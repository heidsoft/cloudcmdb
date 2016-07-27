package org.cmdbuild.model.widget.customform;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement
public class Attribute {

	private static final String //
			DESCRIPTION = "description", //
			EDITOR_TYPE = "editorType", //
			FILTER = "filter", //
			HIDDEN = "hidden", //
			LENGTH = "length", //
			LOOKUP_TYPE = "lookupType", //
			MANDATORY = "mandatory", //
			NAME = "name", //
			PRECISION = "precision", //
			SCALE = "scale", //
			SHOW_COLUMN = "showColumn", //
			TARGET = "target", //
			TARGET_CLASS = "targetClass", //
			TYPE = "type", //
			UNIQUE = "unique", //
			WRITABLE = "writable";

	@XmlRootElement
	public static class Target {

		private String name;
		private String type;

		@XmlAttribute(name = NAME)
		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		@XmlAttribute(name = TYPE)
		public String getType() {
			return type;
		}

		public void setType(final String type) {
			this.type = type;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof Attribute.Filter)) {
				return false;
			}
			final Target other = Target.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.name, other.name) //
					.append(this.type, other.type) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(name) //
					.append(type) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	@XmlRootElement
	public static class Filter {

		private static final String //
				CONTEXT = "context", //
				EXPRESSION = "expression";

		private String expression;
		private Map<String, String> context;

		@XmlAttribute(name = EXPRESSION)
		public String getExpression() {
			return expression;
		}

		public void setExpression(final String text) {
			this.expression = text;
		}

		@XmlElementWrapper(name = CONTEXT)
		public Map<String, String> getContext() {
			return context;
		}

		public void setContext(final Map<String, String> context) {
			this.context = context;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof Attribute.Filter)) {
				return false;
			}
			final Filter other = Filter.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.expression, other.expression) //
					.append(this.context, other.context) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(expression) //
					.append(context) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	private String type;
	private String name;
	private String description;
	private boolean unique;
	private boolean mandatory;
	private boolean writable = true;
	private boolean showColumn = true;
	private boolean hidden;
	private Long precision;
	private Long scale;
	private Long length;
	private String editorType;
	private Target target;
	private String lookupType;
	private Filter filter;

	@XmlAttribute(name = TYPE)
	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	@XmlAttribute(name = NAME)
	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@XmlAttribute(name = DESCRIPTION)
	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	@XmlAttribute(name = UNIQUE)
	public boolean isUnique() {
		return unique;
	}

	public void setUnique(final boolean unique) {
		this.unique = unique;
	}

	@XmlAttribute(name = MANDATORY)
	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(final boolean mandatory) {
		this.mandatory = mandatory;
	}

	@XmlAttribute(name = WRITABLE)
	public boolean isWritable() {
		return writable;
	}

	public void setWritable(final boolean writable) {
		this.writable = writable;
	}

	@XmlAttribute(name = SHOW_COLUMN)
	public boolean isShowColumn() {
		return showColumn;
	}

	public void setShowColumn(final boolean showColumn) {
		this.showColumn = showColumn;
	}

	@XmlAttribute(name = HIDDEN)
	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(final boolean hidden) {
		this.hidden = hidden;
	}

	@XmlAttribute(name = PRECISION)
	public Long getPrecision() {
		return precision;
	}

	public void setPrecision(final Long precision) {
		this.precision = precision;
	}

	@XmlAttribute(name = SCALE)
	public Long getScale() {
		return scale;
	}

	public void setScale(final Long scale) {
		this.scale = scale;
	}

	@XmlAttribute(name = LENGTH)
	public Long getLength() {
		return length;
	}

	public void setLength(final Long length) {
		this.length = length;
	}

	@XmlAttribute(name = EDITOR_TYPE)
	public String getEditorType() {
		return editorType;
	}

	public void setEditorType(final String editorType) {
		this.editorType = editorType;
	}

	/**
	 * @deprecated use {@link getTarget()} instead.
	 */
	@Deprecated
	@XmlAttribute(name = TARGET_CLASS)
	public String getTargetClass() {
		return (target == null) ? null : target.getName();
	}

	/**
	 * @deprecated use {@link setTarget(String)} instead.
	 */
	@Deprecated
	public void setTargetClass(final String targetClass) {
		final String oldType = (target == null) ? null : target.getType();
		this.target = new Target() {
			{
				setName(targetClass);
				setType(oldType);
			}
		};
	}

	@XmlAttribute(name = TARGET)
	public Target getTarget() {
		return target;
	}

	public void setTarget(final Target target) {
		this.target = target;
	}

	@XmlAttribute(name = LOOKUP_TYPE)
	public String getLookupType() {
		return lookupType;
	}

	public void setLookupType(final String lookupType) {
		this.lookupType = lookupType;
	}

	@XmlElement(name = FILTER, nillable = true)
	public Attribute.Filter getFilter() {
		return filter;
	}

	public void setFilter(final Attribute.Filter filter) {
		this.filter = filter;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Attribute)) {
			return false;
		}

		final Attribute other = Attribute.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.type, other.type) //
				.append(this.name, other.name) //
				.append(this.description, other.description) //
				.append(this.unique, other.unique) //
				.append(this.mandatory, other.mandatory) //
				.append(this.writable, other.writable) //
				.append(this.precision, other.precision) //
				.append(this.scale, other.scale) //
				.append(this.length, other.length) //
				.append(this.editorType, other.editorType) //
				.append(this.target, other.target) //
				.append(this.lookupType, other.lookupType) //
				.append(this.filter, other.filter) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(type) //
				.append(name) //
				.append(description) //
				.append(unique) //
				.append(mandatory) //
				.append(writable) //
				.append(precision) //
				.append(scale) //
				.append(length) //
				.append(editorType) //
				.append(target) //
				.append(lookupType) //
				.append(filter) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return reflectionToString(this, SHORT_PREFIX_STYLE);
	}

}