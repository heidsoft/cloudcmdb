package org.cmdbuild.data.store.lookup;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.data.store.lookup.LookupType.LookupTypeBuilder;
import org.cmdbuild.services.localization.LocalizableStorableVisitor;

public final class LookupImpl implements Lookup {

	public static class LookupBuilder implements org.apache.commons.lang3.builder.Builder<LookupImpl> {

		private Long id;
		private String code;
		private String description;
		private String notes;
		private LookupType type;
		private Integer number = 0;
		private boolean active;
		private boolean isDefault;
		private Long parentId;
		private Lookup parent;
		private String translationUuid;

		/**
		 * instantiate using {@link LookupImpl#newInstance()}
		 */
		private LookupBuilder() {
		}

		public LookupImpl.LookupBuilder withId(final Long value) {
			this.id = value;
			return this;
		}

		public LookupImpl.LookupBuilder withCode(final String value) {
			this.code = value;
			return this;
		}

		public LookupImpl.LookupBuilder withDescription(final String value) {
			this.description = value;
			return this;
		}

		public LookupBuilder withNotes(final String value) {
			this.notes = value;
			return this;
		}

		public LookupImpl.LookupBuilder withType(final LookupTypeBuilder builder) {
			return withType(builder.build());
		}

		public LookupImpl.LookupBuilder withType(final LookupType value) {
			this.type = value;
			return this;
		}

		public LookupImpl.LookupBuilder withType(final org.apache.commons.lang3.builder.Builder<LookupType> value) {
			this.type = value.build();
			return this;
		}

		public LookupImpl.LookupBuilder withNumber(final Integer value) {
			this.number = value;
			return this;
		}

		public LookupImpl.LookupBuilder withActiveStatus(final boolean value) {
			this.active = value;
			return this;
		}

		public LookupImpl.LookupBuilder withDefaultStatus(final boolean value) {
			this.isDefault = value;
			return this;
		}

		public LookupImpl.LookupBuilder withParentId(final Long value) {
			this.parentId = value;
			return this;
		}

		public LookupImpl.LookupBuilder withParent(final Lookup value) {
			this.parentId = value.getId();
			this.parent = value;
			return this;
		}

		public LookupImpl.LookupBuilder withUuid(final String translationUuid) {
			this.translationUuid = translationUuid;
			return this;
		}

		@Override
		public LookupImpl build() {
			return new LookupImpl(this);
		}

	}

	public static LookupImpl.LookupBuilder newInstance() {
		return new LookupBuilder();
	}

	private Long id;
	private final String code;
	private final String description;
	private final String notes;
	private final LookupType type;
	private final Integer number;
	private final boolean active;
	private final boolean isDefault;
	private final Long parentId;
	private final Lookup parent;
	private final String translationUuid;

	private final transient String toString;

	@Override
	public String code() {
		return code;
	}

	@Override
	public String description() {
		return description;
	}

	@Override
	public String notes() {
		return notes;
	}

	@Override
	public LookupType type() {
		return type;
	}

	@Override
	public Integer number() {
		return number;
	}

	@Override
	public boolean active() {
		return active;
	}

	@Override
	public boolean isDefault() {
		return isDefault;
	}

	@Override
	public Long parentId() {
		return parentId;
	}

	@Override
	public Lookup parent() {
		return parent;
	}

	@Override
	public String uuid() {
		return translationUuid;
	}

	private LookupImpl(final LookupBuilder builder) {
		this.id = builder.id;
		this.code = builder.code;
		this.description = builder.description;
		this.notes = builder.notes;
		this.type = builder.type;
		this.number = builder.number;
		this.active = builder.active;
		this.isDefault = builder.isDefault;
		this.parentId = builder.parentId;
		this.parent = builder.parent;
		this.translationUuid = builder.translationUuid;
		this.toString = ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
	}

	@Override
	public String getIdentifier() {
		return id.toString();
	}

	@Override
	public String getTranslationUuid() {
		return translationUuid;
	}

	@Override
	public String toString() {
		return toString;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(final Long id) {
		this.id = id;
	}

	// FIXME Do I really need it?
	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void accept(final LocalizableStorableVisitor visitor) {
		visitor.visit(this);
	}

}
