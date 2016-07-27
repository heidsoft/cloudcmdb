package org.cmdbuild.model.data;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.Builder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Domain {

	public static class DomainBuilder implements Builder<Domain> {

		private String name;
		private Long idClass1;
		private Long idClass2;
		private String description;
		private String descriptionDirect;
		private String descriptionInverse;
		private String cardinality;
		private boolean masterDetail;
		private String masterDetailDescription;
		private boolean active;
		private Iterable<String> disabled1;
		private Iterable<String> disabled2;

		private DomainBuilder() {
			// use factory method
		}

		public DomainBuilder withName(final String name) {
			this.name = trim(name);
			return this;
		}

		public DomainBuilder withIdClass1(final long idClass1) {
			this.idClass1 = idClass1;
			return this;
		}

		public DomainBuilder withIdClass2(final long idClass2) {
			this.idClass2 = idClass2;
			return this;
		}

		public DomainBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public DomainBuilder withCardinality(final String cardinality) {
			this.cardinality = cardinality;
			return this;
		}

		public DomainBuilder withDirectDescription(final String descriptionDirect) {
			this.descriptionDirect = descriptionDirect;
			return this;
		}

		public DomainBuilder withInverseDescription(final String descriptionInverse) {
			this.descriptionInverse = descriptionInverse;
			return this;
		}

		public DomainBuilder thatIsMasterDetail(final boolean isMasterDetail) {
			this.masterDetail = isMasterDetail;
			return this;
		}

		public DomainBuilder withMasterDetailDescription(final String masterDetailLabel) {
			this.masterDetailDescription = masterDetailLabel;
			return this;
		}

		public DomainBuilder thatIsActive(final boolean isActive) {
			this.active = isActive;
			return this;
		}

		public DomainBuilder withDisabled1(final Iterable<String> disabled1) {
			this.disabled1 = disabled1;
			return this;
		}

		public DomainBuilder withDisabled2(final Iterable<String> disabled2) {
			this.disabled2 = disabled2;
			return this;
		}

		@Override
		public Domain build() {
			Validate.isTrue(isNotBlank(name), "invalid name");
			Validate.isTrue(idClass1 != null && idClass1 > 0, "invalid id for class 1");
			Validate.isTrue(idClass2 != null && idClass2 > 0, "invalid id for class 2");
			description = defaultIfBlank(description, name);
			return new Domain(this);
		}

	}

	private final String name;
	private final long idClass1;
	private final long idClass2;
	private final String description;
	private final String descriptionDirect;
	private final String descriptionInverse;
	private final String cardinality;
	private final boolean masterDetail;
	private final String masterDetailDescription;
	private final boolean active;
	private final Iterable<String> disabled1;
	private final Iterable<String> disabled2;

	public static DomainBuilder newDomain() {
		return new DomainBuilder();
	}

	private Domain(final DomainBuilder builder) {
		this.name = builder.name;
		this.idClass1 = builder.idClass1;
		this.idClass2 = builder.idClass2;
		this.description = builder.description;
		this.descriptionDirect = builder.descriptionDirect;
		this.descriptionInverse = builder.descriptionInverse;
		this.cardinality = builder.cardinality;
		this.masterDetail = builder.masterDetail;
		this.masterDetailDescription = builder.masterDetailDescription;
		this.active = builder.active;
		this.disabled1 = builder.disabled1;
		this.disabled2 = builder.disabled2;
	}

	public String getName() {
		return name;
	}

	public long getIdClass1() {
		return idClass1;
	}

	public long getIdClass2() {
		return idClass2;
	}

	public String getDescription() {
		return description;
	}

	public String getDirectDescription() {
		return descriptionDirect;
	}

	public String getInverseDescription() {
		return descriptionInverse;
	}

	public String getCardinality() {
		return cardinality;
	}

	public boolean isMasterDetail() {
		return masterDetail;
	}

	public String getMasterDetailDescription() {
		return masterDetailDescription;
	}

	public boolean isActive() {
		return active;
	}

	public Iterable<String> getDisabled1() {
		return disabled1;
	}

	public Iterable<String> getDisabled2() {
		return disabled2;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
	}

}
