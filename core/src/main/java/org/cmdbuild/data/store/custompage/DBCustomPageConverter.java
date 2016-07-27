package org.cmdbuild.data.store.custompage;

import static com.google.common.collect.Maps.newHashMap;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;

import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.dao.BaseStorableConverter;

public class DBCustomPageConverter extends BaseStorableConverter<DBCustomPage> {

	public static final String CLASSNAME = "_CustomPage";

	private static final String NAME = CODE_ATTRIBUTE;
	private static final String DESCRIPTION = DESCRIPTION_ATTRIBUTE;

	private static class CMCardAdapter implements DBCustomPage {

		private final CMCard delegate;

		public CMCardAdapter(final CMCard delegate) {
			this.delegate = delegate;
		}

		@Override
		public String getIdentifier() {
			return getId().toString();
		}

		@Override
		public Long getId() {
			return delegate.getId();
		}

		@Override
		public String getName() {
			return delegate.get(NAME, String.class);
		}

		@Override
		public String getDescription() {
			return delegate.get(DESCRIPTION, String.class);
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof DBCustomPage)) {
				return false;
			}
			final DBCustomPage other = DBCustomPage.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.getIdentifier(), other.getIdentifier()) //
					.append(this.getId(), other.getId()) //
					.append(this.getName(), other.getName()) //
					.append(this.getDescription(), other.getDescription()) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(getIdentifier()) //
					.append(getId()) //
					.append(getName()) //
					.append(getDescription()) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	@Override
	public String getClassName() {
		return CLASSNAME;
	}

	@Override
	public DBCustomPage convert(final CMCard card) {
		return new CMCardAdapter(card);
	}

	@Override
	public Map<String, Object> getValues(final DBCustomPage storable) {
		final Map<String, Object> map = newHashMap();
		map.put(NAME, storable.getName());
		map.put(DESCRIPTION, storable.getDescription());
		return map;
	}

}
