package org.cmdbuild.service.rest.v2.model;

import static org.cmdbuild.service.rest.v2.constants.Serialization.DESCRIPTION;
import static org.cmdbuild.service.rest.v2.constants.Serialization.PARENT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.POSITIONS;
import static org.cmdbuild.service.rest.v2.constants.Serialization.REFERENCES;
import static org.cmdbuild.service.rest.v2.constants.Serialization.TOTAL;

import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement
public class DetailResponseMetadata extends AbstractModel {

	@XmlRootElement
	public static class Reference extends AbstractModel {

		private String description;
		private Long parent;

		Reference() {
			// package visibility
		}

		@XmlElement(name = DESCRIPTION)
		public String getDescription() {
			return description;
		}

		void setDescription(final String description) {
			this.description = description;
		}

		@XmlElement(name = PARENT)
		public Long getParent() {
			return parent;
		}

		void setParent(final Long parent) {
			this.parent = parent;
		}

		@Override
		protected boolean doEquals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof Reference)) {
				return false;
			}
			final Reference other = Reference.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.description, other.description) //
					.append(this.parent, other.parent) //
					.isEquals();
		}

		@Override
		protected int doHashCode() {
			return new HashCodeBuilder() //
					.append(this.description) //
					.append(this.parent) //
					.toHashCode();
		}

	}

	private Long total;
	private Map<Long, Long> positions;
	private Map<Long, Reference> references;

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

	@XmlElement(name = REFERENCES)
	public Map<Long, Reference> getReferences() {
		return references;
	}

	void setReferences(final Map<Long, Reference> references) {
		this.references = references;
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
				.append(this.references, other.references) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(this.total) //
				.append(this.positions) //
				.append(this.references) //
				.toHashCode();
	}

}
