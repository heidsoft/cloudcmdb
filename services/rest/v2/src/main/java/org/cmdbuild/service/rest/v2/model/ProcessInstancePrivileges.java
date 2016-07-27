package org.cmdbuild.service.rest.v2.model;

import static org.cmdbuild.service.rest.v2.constants.Serialization.STOPPABLE;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement
public class ProcessInstancePrivileges extends AbstractModel {

	private boolean stoppable;

	ProcessInstancePrivileges() {
		// package visibility
	}

	@XmlAttribute(name = STOPPABLE)
	public boolean isStoppable() {
		return stoppable;
	}

	void setStoppable(final boolean stoppable) {
		this.stoppable = stoppable;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof EmailTemplate)) {
			return false;
		}
		final ProcessInstancePrivileges other = ProcessInstancePrivileges.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.stoppable, other.stoppable) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(stoppable) //
				.toHashCode();
	}

}
