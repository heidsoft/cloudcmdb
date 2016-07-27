package org.cmdbuild.service.rest.v1.model;

import static org.cmdbuild.service.rest.v1.constants.Serialization.CARDINALITY;
import static org.cmdbuild.service.rest.v1.constants.Serialization.DESCRIPTION_DIRECT;
import static org.cmdbuild.service.rest.v1.constants.Serialization.DESCRIPTION_INVERSE;
import static org.cmdbuild.service.rest.v1.constants.Serialization.DESCRIPTION_MASTER_DETAIL;
import static org.cmdbuild.service.rest.v1.constants.Serialization.DESTINATION;
import static org.cmdbuild.service.rest.v1.constants.Serialization.DESTINATION_PROCESS;
import static org.cmdbuild.service.rest.v1.constants.Serialization.SOURCE;
import static org.cmdbuild.service.rest.v1.constants.Serialization.SOURCE_PROCESS;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement
public class DomainWithFullDetails extends DomainWithBasicDetails {

	private String source;
	private boolean sourceProcess;
	private String destination;
	private boolean destinationProcess;
	private String cardinality;
	private String descriptionDirect;
	private String descriptionInverse;
	private String descriptionMasterDetail;

	DomainWithFullDetails() {
		// package visibility
	}

	@XmlAttribute(name = SOURCE)
	public String getSource() {
		return source;
	}

	void setSource(final String source) {
		this.source = source;
	}

	@XmlAttribute(name = SOURCE_PROCESS)
	public boolean isSourceProcess() {
		return sourceProcess;
	}

	void setSourceProcess(final boolean sourceProcess) {
		this.sourceProcess = sourceProcess;
	}

	@XmlAttribute(name = DESTINATION)
	public String getDestination() {
		return destination;
	}

	void setDestination(final String source) {
		this.destination = source;
	}

	@XmlAttribute(name = DESTINATION_PROCESS)
	public boolean isDestinationProcess() {
		return destinationProcess;
	}

	void setDestinationProcess(final boolean destinationProcess) {
		this.destinationProcess = destinationProcess;
	}

	@XmlAttribute(name = CARDINALITY)
	public String getCardinality() {
		return cardinality;
	}

	void setCardinality(final String cardinality) {
		this.cardinality = cardinality;
	}

	@XmlAttribute(name = DESCRIPTION_DIRECT)
	public String getDescriptionDirect() {
		return descriptionDirect;
	}

	void setDescriptionDirect(final String descriptionDirect) {
		this.descriptionDirect = descriptionDirect;
	}

	@XmlAttribute(name = DESCRIPTION_INVERSE)
	public String getDescriptionInverse() {
		return descriptionInverse;
	}

	void setDescriptionInverse(final String descriptionInverse) {
		this.descriptionInverse = descriptionInverse;
	}

	@XmlAttribute(name = DESCRIPTION_MASTER_DETAIL)
	public String getDescriptionMasterDetail() {
		return descriptionMasterDetail;
	}

	void setDescriptionMasterDetail(final String descriptionMasterDetail) {
		this.descriptionMasterDetail = descriptionMasterDetail;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof DomainWithFullDetails)) {
			return false;
		}

		final DomainWithFullDetails other = DomainWithFullDetails.class.cast(obj);
		return super.doEquals(obj) && new EqualsBuilder() //
				.append(this.source, other.source) //
				.append(this.destination, other.destination) //
				.append(this.cardinality, other.cardinality) //
				.append(this.descriptionDirect, other.descriptionDirect) //
				.append(this.descriptionInverse, other.descriptionInverse) //
				.append(this.descriptionMasterDetail, other.descriptionMasterDetail) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(super.doHashCode()) //
				.append(source) //
				.append(destination) //
				.append(cardinality) //
				.append(descriptionDirect) //
				.append(descriptionInverse) //
				.append(descriptionMasterDetail) //
				.toHashCode();
	}

}
