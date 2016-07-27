package org.cmdbuild.service.rest.v1.model;

import static org.cmdbuild.service.rest.v1.constants.Serialization.VALUES;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.service.rest.v1.model.adapter.CardAdapter;

@XmlRootElement
@XmlJavaTypeAdapter(CardAdapter.class)
public class Card extends AbstractCardModel {

	private Values values;

	Card() {
		// package visibility
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

		if (!(obj instanceof Card)) {
			return false;
		}

		final Card other = Card.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.getType(), other.getType()) //
				.append(this.getId(), other.getId()) //
				.append(this.values, other.values) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(getType()) //
				.append(getId()) //
				.append(values) //
				.toHashCode();
	}

}
