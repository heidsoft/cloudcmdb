package org.cmdbuild.dao.entrytype;

import static com.google.common.base.Splitter.on;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trim;

import java.util.List;

import org.apache.commons.lang3.builder.Builder;

import com.google.common.collect.Lists;

public class DBDomain extends DBEntryType implements CMDomain {

	public static class DomainMetadata extends EntryTypeMetadata {

		public static final String CARDINALITY = BASE_NS + "cardinality";
		public static final String CLASS_1 = BASE_NS + "class1";
		public static final String CLASS_2 = BASE_NS + "class2";
		public static final String DESCRIPTION_1 = BASE_NS + "description1";
		public static final String DESCRIPTION_2 = BASE_NS + "description2";
		public static final String MASTERDETAIL = BASE_NS + "masterdetail";
		public static final String MASTERDETAIL_DESCRIPTION = BASE_NS + "masterdetail.label";
		public static final String DISABLED_1 = BASE_NS + "disabled.1";
		public static final String DISABLED_2 = BASE_NS + "disabled.2";
		public static final String DISABLED_SEPARATOR = ",";

		private static final Iterable<String> NO_DISABLED = emptyList();

		public String getDescription1() {
			return get(DESCRIPTION_1);
		}

		public String getDescription2() {
			return get(DESCRIPTION_2);
		}

		public String getCardinality() {
			return get(CARDINALITY);
		}

		public boolean isMasterDetail() {
			return Boolean.parseBoolean(get(MASTERDETAIL));
		}

		public String getMasterDetailDescription() {
			return get(MASTERDETAIL_DESCRIPTION);
		}

		public Iterable<String> getDisabled1() {
			return disabled(get(DISABLED_1));
		}

		public Iterable<String> getDisabled2() {
			return disabled(get(DISABLED_2));
		}

		private Iterable<String> disabled(final String value) {
			return isBlank(value) ? NO_DISABLED : on(DISABLED_SEPARATOR).split(trim(value));
		}

	}

	public static class DBDomainBuilder implements Builder<DBDomain> {

		private final List<DBAttribute> attributes;

		private CMIdentifier identifier;
		private Long id;
		private DomainMetadata metadata;
		private DBClass class1;
		private DBClass class2;

		private DBDomainBuilder() {
			metadata = new DomainMetadata();
			attributes = Lists.newArrayList();
		}

		public DBDomainBuilder withIdentifier(final CMIdentifier identifier) {
			this.identifier = identifier;
			return this;
		}

		public DBDomainBuilder withId(final Long id) {
			this.id = id;
			return this;
		}

		public DBDomainBuilder withAllMetadata(final DomainMetadata metadata) {
			this.metadata = metadata;
			return this;
		}

		public DBDomainBuilder withAllAttributes(final List<DBAttribute> attributes) {
			this.attributes.addAll(attributes);
			return this;
		}

		public DBDomainBuilder withAttribute(final DBAttribute attribute) {
			this.attributes.add(attribute);
			return this;
		}

		@Deprecated
		public DBDomainBuilder withClass1(final DBClass dbClass) {
			this.class1 = dbClass;
			return this;
		}

		@Deprecated
		public DBDomainBuilder withClass2(final DBClass dbClass) {
			this.class2 = dbClass;
			return this;
		}

		@Override
		public DBDomain build() {
			return new DBDomain(this);
		}

	}

	public static DBDomainBuilder newDomain() {
		return new DBDomainBuilder();
	}

	private final DomainMetadata metadata;

	@Deprecated
	private final DBClass class1;
	@Deprecated
	private final DBClass class2;

	private DBDomain(final DBDomainBuilder builder) {
		super(builder.identifier, builder.id, builder.attributes);
		this.metadata = builder.metadata;
		this.class1 = builder.class1;
		this.class2 = builder.class2;
	}

	@Override
	public void accept(final CMEntryTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected final DomainMetadata meta() {
		return metadata;
	}

	@Override
	public String toString() {
		return String.format("[Domain %s]", getIdentifier().getLocalName());
	}

	@Override
	public final String getPrivilegeId() {
		return String.format("Domain:%d", getId());
	}

	@Override
	public DBClass getClass1() {
		if (class1 == null) {
			throw new IllegalStateException();
		}
		return class1;
	}

	@Override
	public DBClass getClass2() {
		if (class2 == null) {
			throw new IllegalStateException();
		}
		return class2;
	}

	@Override
	public String getDescription1() {
		return meta().getDescription1();
	}

	@Override
	public String getDescription2() {
		return meta().getDescription2();
	}

	@Override
	public String getCardinality() {
		return meta().getCardinality();
	}

	@Override
	public boolean isMasterDetail() {
		return meta().isMasterDetail();
	}

	@Override
	public String getMasterDetailDescription() {
		return meta().getMasterDetailDescription();
	}

	@Override
	public boolean holdsHistory() {
		return true;
	}

	@Override
	public Iterable<String> getDisabled1() {
		return meta().getDisabled1();
	}

	@Override
	public Iterable<String> getDisabled2() {
		return meta().getDisabled2();
	}

}
