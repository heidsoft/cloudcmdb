package org.cmdbuild.service.rest.v2.model;

import static org.cmdbuild.service.rest.v2.constants.Serialization.DESTINATION;
import static org.cmdbuild.service.rest.v2.constants.Serialization.SOURCE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.VALUES;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.service.rest.v2.model.adapter.RelationAdapter;

@XmlRootElement
@XmlJavaTypeAdapter(RelationAdapter.class)
public class Relation extends AbstractCardModel {

	private Card source;
	private Card destination;
	private Values values;

	Relation() {
		// package visibility
	}

	@XmlElement(name = SOURCE)
	public Card getSource() {
		return source;
	}

	void setSource(final Card source) {
		this.source = source;
	}

	@XmlElement(name = DESTINATION)
	public Card getDestination() {
		return destination;
	}

	void setDestination(final Card destination) {
		this.destination = destination;
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

		if (!(obj instanceof Relation)) {
			return false;
		}

		final Relation other = Relation.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.getType(), other.getType()) //
				.append(this.getId(), other.getId()) //
				.append(this.source, other.source) //
				.append(this.destination, other.destination) //
				.append(this.values, other.values) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(getType()) //
				.append(getId()) //
				.append(this.source) //
				.append(this.destination) //
				.append(values) //
				.toHashCode();
	}

}
