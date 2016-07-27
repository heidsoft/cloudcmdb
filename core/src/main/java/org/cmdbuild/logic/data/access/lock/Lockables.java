package org.cmdbuild.logic.data.access.lock;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Optional;

public class Lockables {

	private static abstract class AbstractLockable implements Lockable {

		@Override
		public abstract Optional<Lockable> parent();

		@Override
		public final boolean equals(final Object obj) {
			return doEquals(obj);
		}

		protected abstract boolean doEquals(Object obj);

		@Override
		public final int hashCode() {
			return doHashCode();
		}

		protected abstract int doHashCode();

		@Override
		public final String toString() {
			return reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	private static class LockableCard extends AbstractLockable {

		private static final Optional<Lockable> ABSENT = absent();

		private final Long id;

		public LockableCard(final Long id) {
			this.id = id;
		}

		@Override
		public Optional<Lockable> parent() {
			return ABSENT;
		}

		@Override
		protected boolean doEquals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof LockableCard)) {
				return false;
			}
			final LockableCard other = LockableCard.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.id, other.id) //
					.isEquals();
		}

		@Override
		protected int doHashCode() {
			return new HashCodeBuilder() //
					.append(LockableCard.class) //
					.append(id) //
					.toHashCode();
		}

	}

	private static class LockableInstanceActivity extends AbstractLockable {

		private final Lockable instance;
		private final String activityId;

		public LockableInstanceActivity(final Lockable instance, final String activityId) {
			this.instance = instance;
			this.activityId = activityId;
		}

		@Override
		public Optional<Lockable> parent() {
			return of(instance);
		}

		@Override
		protected boolean doEquals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof LockableInstanceActivity)) {
				return false;
			}
			final LockableInstanceActivity other = LockableInstanceActivity.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.instance, other.instance) //
					.append(this.activityId, other.activityId) //
					.isEquals();
		}

		@Override
		protected int doHashCode() {
			return new HashCodeBuilder() //
					.append(LockableInstanceActivity.class) //
					.append(instance) //
					.append(activityId) //
					.toHashCode();
		}

	}

	public static Lockable card(final Long id) {
		// TODO use cache
		return new LockableCard(id);
	}

	public static Lockable instanceActivity(final Long instanceId, final String activityId) {
		// TODO use cache
		return new LockableInstanceActivity(card(instanceId), activityId);
	}

	private Lockables() {
		// prevents instantiation
	}

}
