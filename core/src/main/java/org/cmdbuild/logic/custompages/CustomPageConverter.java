package org.cmdbuild.logic.custompages;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.data.store.custompage.DBCustomPage;

import com.google.common.base.Converter;

public class CustomPageConverter extends Converter<CustomPage, DBCustomPage> {

	private static class CustomPageAdapter implements DBCustomPage {

		private final CustomPage delegate;

		public CustomPageAdapter(final CustomPage delegate) {
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
			return delegate.getName();
		}

		@Override
		public String getDescription() {
			return delegate.getDescription();
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

	private static class DBCustomPageAdapter implements CustomPage {

		private final DBCustomPage delegate;

		public DBCustomPageAdapter(final DBCustomPage delegate) {
			this.delegate = delegate;
		}

		@Override
		public Long getId() {
			return delegate.getId();
		}

		@Override
		public String getName() {
			return delegate.getName();
		}

		@Override
		public String getDescription() {
			return delegate.getDescription();
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof CustomPage)) {
				return false;
			}
			final CustomPage other = CustomPage.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.getId(), other.getId()) //
					.append(this.getName(), other.getName()) //
					.append(this.getDescription(), other.getDescription()) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
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
	protected DBCustomPage doForward(final CustomPage a) {
		return new CustomPageAdapter(a);
	}

	@Override
	protected CustomPage doBackward(final DBCustomPage b) {
		return new DBCustomPageAdapter(b);
	}

}