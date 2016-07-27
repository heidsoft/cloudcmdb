package org.cmdbuild.service.rest.v2.model;

import static com.google.common.collect.Lists.newArrayList;
import static org.cmdbuild.service.rest.v2.constants.Serialization.DATA;
import static org.cmdbuild.service.rest.v2.constants.Serialization.RESPONSE_METADATA;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement
public class ResponseMultiple<T> extends AbstractModel {

	private Collection<T> elements;
	private DetailResponseMetadata metadata;

	ResponseMultiple() {
		// package visibility
	}

	@XmlElement(name = DATA)
	@JsonProperty(DATA)
	public Collection<T> getElements() {
		return elements;
	}

	void setElements(final Iterable<T> elements) {
		this.elements = newArrayList(elements);
	}

	@XmlElement(name = RESPONSE_METADATA, type = DetailResponseMetadata.class)
	@JsonProperty(RESPONSE_METADATA)
	public DetailResponseMetadata getMetadata() {
		return metadata;
	}

	void setMetadata(final DetailResponseMetadata metadata) {
		this.metadata = metadata;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof ResponseMultiple)) {
			return false;
		}

		@SuppressWarnings("unchecked")
		final ResponseMultiple<T> other = ResponseMultiple.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.elements, other.elements) //
				.append(this.metadata, other.metadata) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(this.elements) //
				.append(this.metadata) //
				.toHashCode();
	}

}
