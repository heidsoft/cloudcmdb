package org.cmdbuild.service.rest.v2.model;

import static org.cmdbuild.service.rest.v2.constants.Serialization.ACTIVE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.DEFAULT_VALUE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.DESCRIPTION;
import static org.cmdbuild.service.rest.v2.constants.Serialization.DISPLAYABLE_IN_LIST;
import static org.cmdbuild.service.rest.v2.constants.Serialization.DOMAIN_NAME;
import static org.cmdbuild.service.rest.v2.constants.Serialization.EDITOR_TYPE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.FILTER;
import static org.cmdbuild.service.rest.v2.constants.Serialization.GROUP;
import static org.cmdbuild.service.rest.v2.constants.Serialization.HIDDEN;
import static org.cmdbuild.service.rest.v2.constants.Serialization.INDEX;
import static org.cmdbuild.service.rest.v2.constants.Serialization.INHERITED;
import static org.cmdbuild.service.rest.v2.constants.Serialization.LENGTH;
import static org.cmdbuild.service.rest.v2.constants.Serialization.LOOKUP_TYPE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.MANDATORY;
import static org.cmdbuild.service.rest.v2.constants.Serialization.METADATA;
import static org.cmdbuild.service.rest.v2.constants.Serialization.NAME;
import static org.cmdbuild.service.rest.v2.constants.Serialization.PARAMS;
import static org.cmdbuild.service.rest.v2.constants.Serialization.PRECISION;
import static org.cmdbuild.service.rest.v2.constants.Serialization.SCALE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.TARGET_CLASS;
import static org.cmdbuild.service.rest.v2.constants.Serialization.TARGET_TYPE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.TEXT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.TYPE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.UNIQUE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.VALUES;
import static org.cmdbuild.service.rest.v2.constants.Serialization.WRITABLE;

import java.util.Collection;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement
public class Attribute extends ModelWithStringId {

	@XmlRootElement
	public static class Filter extends AbstractModel {

		private String text;
		private Map<String, String> params;

		Filter() {
			// package visibility
		}

		@XmlAttribute(name = TEXT)
		public String getText() {
			return text;
		}

		void setText(final String text) {
			this.text = text;
		}

		@XmlElementWrapper(name = PARAMS)
		public Map<String, String> getParams() {
			return params;
		}

		void setParams(final Map<String, String> params) {
			this.params = params;
		}

		@Override
		protected boolean doEquals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof Filter)) {
				return false;
			}
			final Filter other = Filter.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.text, other.text) //
					.append(this.params, other.params) //
					.isEquals();
		}

		@Override
		protected int doHashCode() {
			return new HashCodeBuilder() //
					.append(text) //
					.append(params) //
					.toHashCode();
		}

	}

	private String type;
	private String name;
	private String description;
	private boolean displayableInList;
	private String domainName;
	private boolean unique;
	private boolean mandatory;
	private boolean inherited;
	private boolean active;
	private Long index;
	private String defaultValue;
	private String group;
	private Long precision;
	private Long scale;
	private String targetClass;
	private String targetType;
	private Long length;
	private String editorType;
	private String lookupTypeName;
	private Filter filter;
	private Collection<String> values;
	private boolean writable;
	private boolean hidden;
	private Map<String, String> metadata;

	Attribute() {
		// package visibility
	}

	@XmlAttribute(name = TYPE)
	public String getType() {
		return type;
	}

	void setType(final String type) {
		this.type = type;
	}

	@XmlAttribute(name = NAME)
	public String getName() {
		return name;
	}

	void setName(final String name) {
		this.name = name;
	}

	@XmlAttribute(name = DESCRIPTION)
	public String getDescription() {
		return description;
	}

	void setDescription(final String description) {
		this.description = description;
	}

	@XmlAttribute(name = DISPLAYABLE_IN_LIST)
	public boolean isDisplayableInList() {
		return displayableInList;
	}

	void setDisplayableInList(final boolean displayableInList) {
		this.displayableInList = displayableInList;
	}

	@XmlAttribute(name = DOMAIN_NAME)
	public String getDomainName() {
		return domainName;
	}

	void setDomainName(final String domainName) {
		this.domainName = domainName;
	}

	@XmlAttribute(name = UNIQUE)
	public boolean isUnique() {
		return unique;
	}

	void setUnique(final boolean unique) {
		this.unique = unique;
	}

	@XmlAttribute(name = MANDATORY)
	public boolean isMandatory() {
		return mandatory;
	}

	void setMandatory(final boolean mandatory) {
		this.mandatory = mandatory;
	}

	@XmlAttribute(name = INHERITED)
	public boolean isInherited() {
		return inherited;
	}

	void setInherited(final boolean inherited) {
		this.inherited = inherited;
	}

	@XmlAttribute(name = ACTIVE)
	public boolean isActive() {
		return active;
	}

	void setActive(final boolean active) {
		this.active = active;
	}

	@XmlAttribute(name = INDEX)
	public Long getIndex() {
		return index;
	}

	void setIndex(final Long index) {
		this.index = index;
	}

	@XmlAttribute(name = DEFAULT_VALUE)
	public String getDefaultValue() {
		return defaultValue;
	}

	void setDefaultValue(final String defaultValue) {
		this.defaultValue = defaultValue;
	}

	@XmlAttribute(name = GROUP)
	public String getGroup() {
		return group;
	}

	void setGroup(final String group) {
		this.group = group;
	}

	@XmlAttribute(name = PRECISION)
	public Long getPrecision() {
		return precision;
	}

	void setPrecision(final Long precision) {
		this.precision = precision;
	}

	@XmlAttribute(name = SCALE)
	public Long getScale() {
		return scale;
	}

	void setScale(final Long scale) {
		this.scale = scale;
	}

	@XmlAttribute(name = TARGET_CLASS)
	public String getTargetClass() {
		return targetClass;
	}

	void setTargetClass(final String targetClass) {
		this.targetClass = targetClass;
	}

	@XmlAttribute(name = TARGET_TYPE)
	public String getTargetType() {
		return targetType;
	}

	void setTargetType(final String targetType) {
		this.targetType = targetType;
	}

	@XmlAttribute(name = LENGTH)
	public Long getLength() {
		return length;
	}

	void setLength(final Long length) {
		this.length = length;
	}

	@XmlAttribute(name = EDITOR_TYPE)
	public String getEditorType() {
		return editorType;
	}

	void setEditorType(final String editorType) {
		this.editorType = editorType;
	}

	@XmlAttribute(name = LOOKUP_TYPE)
	public String getLookupType() {
		return lookupTypeName;
	}

	void setLookupType(final String lookupType) {
		this.lookupTypeName = lookupType;
	}

	@XmlElement(name = FILTER, nillable = true)
	public Filter getFilter() {
		return filter;
	}

	void setFilter(final Filter filter) {
		this.filter = filter;
	}

	@XmlElement(name = VALUES, nillable = true)
	public Collection<String> getValues() {
		return values;
	}

	void setValues(final Collection<String> values) {
		this.values = values;
	}

	@XmlAttribute(name = WRITABLE)
	public boolean isWritable() {
		return writable;
	}

	void setWritable(final boolean writable) {
		this.writable = writable;
	}

	@XmlAttribute(name = HIDDEN)
	public boolean isHidden() {
		return hidden;
	}

	void setHidden(final boolean hidden) {
		this.hidden = hidden;
	}

	@XmlElement(name = METADATA, nillable = true)
	public Map<String, String> getMetadata() {
		return metadata;
	}

	void setMetadata(final Map<String, String> metadata) {
		this.metadata = metadata;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Attribute)) {
			return false;
		}

		final Attribute other = Attribute.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.getId(), other.getId()) //
				.append(this.type, other.type) //
				.append(this.name, other.name) //
				.append(this.description, other.description) //
				.append(this.displayableInList, other.displayableInList) //
				.append(this.unique, other.unique) //
				.append(this.mandatory, other.mandatory) //
				.append(this.inherited, other.inherited) //
				.append(this.active, other.active) //
				.append(this.index, other.index) //
				.append(this.defaultValue, other.defaultValue) //
				.append(this.group, other.group) //
				.append(this.precision, other.precision) //
				.append(this.scale, other.scale) //
				.append(this.targetClass, other.targetClass) //
				.append(this.targetType, other.targetType) //
				.append(this.length, other.length) //
				.append(this.editorType, other.editorType) //
				.append(this.lookupTypeName, other.lookupTypeName) //
				.append(this.filter, other.filter) //
				.append(this.writable, other.writable) //
				.append(this.hidden, other.hidden) //
				.append(this.metadata, other.metadata) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(getId()) //
				.append(type) //
				.append(name) //
				.append(description) //
				.append(displayableInList) //
				.append(unique) //
				.append(mandatory) //
				.append(inherited) //
				.append(active) //
				.append(index) //
				.append(defaultValue) //
				.append(group) //
				.append(precision) //
				.append(scale) //
				.append(targetClass) //
				.append(targetType) //
				.append(length) //
				.append(editorType) //
				.append(lookupTypeName) //
				.append(filter) //
				.append(writable) //
				.append(hidden) //
				.append(metadata) //
				.toHashCode();
	}

}
