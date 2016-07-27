package org.cmdbuild.service.rest.v1.model;

import static org.cmdbuild.service.rest.v1.constants.Serialization.DATA;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement
public class ResponseSingle<T> extends AbstractModel {

	private T element;

	ResponseSingle() {
		// package visibility
	}

	@XmlElement(name = DATA)
	@JsonProperty(DATA)
	public T getElement() {
		return element;
	}

	void setElement(final T element) {
		this.element = element;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof ResponseSingle)) {
			return false;
		}

		@SuppressWarnings("unchecked")
		final ResponseSingle<T> other = ResponseSingle.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.element, other.element) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(this.element) //
				.toHashCode();
	}

}
