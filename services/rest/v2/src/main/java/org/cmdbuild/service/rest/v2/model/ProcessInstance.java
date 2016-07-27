package org.cmdbuild.service.rest.v2.model;

import static org.cmdbuild.service.rest.v2.constants.Serialization.NAME;
import static org.cmdbuild.service.rest.v2.constants.Serialization.STATUS;
import static org.cmdbuild.service.rest.v2.constants.Serialization.VALUES;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.service.rest.v2.model.adapter.ProcessInstanceAdapter;

@XmlRootElement
@XmlJavaTypeAdapter(ProcessInstanceAdapter.class)
public class ProcessInstance extends AbstractCardModel {

	private String name;
	private Long status;
	private Values values;

	ProcessInstance() {
		// package visibility
	}

	@XmlAttribute(name = NAME)
	public String getName() {
		return name;
	}

	void setName(final String name) {
		this.name = name;
	}

	@XmlAttribute(name = STATUS)
	public Long getStatus() {
		return status;
	}

	void setStatus(final Long status) {
		this.status = status;
	}

	@XmlElement(name = VALUES)
	public Values getValues() {
		return values;
	}

	void setValues(final Values values) {
		this.values = values;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof ProcessInstance)) {
			return false;
		}

		final ProcessInstance other = ProcessInstance.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.getType(), other.getType()) //
				.append(this.getId(), other.getId()) //
				.append(this.name, other.name) //
				.append(this.status, other.status) //
				.append(this.values, other.values) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(getType()) //
				.append(getId()) //
				.append(name) //
				.append(status) //
				.append(values) //
				.toHashCode();
	}

}
