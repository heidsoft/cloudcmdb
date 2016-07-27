package org.cmdbuild.service.rest.v1.model;

import static org.cmdbuild.service.rest.v1.constants.Serialization.POSITIONS;
import static org.cmdbuild.service.rest.v1.constants.Serialization.TOTAL;

import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement
public class DetailResponseMetadata extends AbstractModel {

	private Long total;
	private Map<Long, Long> positions;

	DetailResponseMetadata() {
		// package visibility
	}

	@XmlAttribute(name = TOTAL)
	public Long getTotal() {
		return total;
	}

	void setTotal(final Long total) {
		this.total = total;
	}

	@XmlElement(name = POSITIONS)
	public Map<Long, Long> getPositions() {
		return positions;
	}

	void setPositions(final Map<Long, Long> positions) {
		this.positions = positions;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof DetailResponseMetadata)) {
			return false;
		}
		final DetailResponseMetadata other = DetailResponseMetadata.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.total, other.total) //
				.append(this.positions, other.positions) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(this.total) //
				.append(this.positions) //
				.toHashCode();
	}

}
