package org.cmdbuild.logic.privileges;

import org.cmdbuild.auth.acl.SerializablePrivilege;
import org.cmdbuild.auth.privileges.constants.PrivilegeMode;

public class PrivilegeInfo {

	public static final String[] EMPTY_ATTRIBUTES_PRIVILEGES = new String[0];

	private final Long groupId;
	private final PrivilegeMode classMode;
	private final SerializablePrivilege privilegedObject;

	private String privilegeFilter;
	private String[] attributesPrivileges;
	private CardEditMode cardEditMode;

	public PrivilegeInfo(final Long groupId, final SerializablePrivilege privilegedObject, final PrivilegeMode mode,
			final CardEditMode cardEditMode) {
		this.groupId = groupId;
		this.classMode = mode;
		this.privilegedObject = privilegedObject;
		this.cardEditMode = cardEditMode;
	}

	public PrivilegeInfo(final Builder builder) {
		this.groupId = builder.groupId;
		this.privilegedObject = builder.privilegedObject;
		this.classMode = builder.classMode;
		this.cardEditMode = builder.cardEditMode;
		this.attributesPrivileges = builder.attributesPrivileges;
		this.privilegeFilter = builder.privilegeFilter;
	}

	public static Builder newInstance() {
		return new Builder();
	}

	public String getPrivilegeFilter() {
		return privilegeFilter;
	}

	public void setPrivilegeFilter(final String privilegeFilter) {
		this.privilegeFilter = privilegeFilter;
	}

	public String[] getAttributesPrivileges() {
		return attributesPrivileges;
	}

	public void setAttributesPrivileges(final String[] attributesPrivileges) {
		this.attributesPrivileges = attributesPrivileges;
	}

	public void setCardEditMode(final CardEditMode cardEditMode) {
		this.cardEditMode = cardEditMode;
	}

	public SerializablePrivilege getPrivilegedObject() {
		return privilegedObject;
	}

	public PrivilegeMode getMode() {
		return classMode;
	}

	public Long getPrivilegedObjectId() {
		return privilegedObject.getId();
	}

	public String getPrivilegedObjectName() {
		return privilegedObject.getName();
	}

	public String getPrivilegedObjectDescription() {
		return privilegedObject.getDescription();
	}

	public Long getGroupId() {
		return groupId;
	}

	public String getPrivilegeId() {
		return privilegedObject.getPrivilegeId();
	}

	public CardEditMode getCardEditMode() {
		return cardEditMode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = prime * result + ((classMode == null) ? 0 : classMode.hashCode());
		result = prime * result + ((privilegedObject == null) ? 0 : privilegedObject.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof PrivilegeInfo)) {
			return false;
		}
		final PrivilegeInfo other = (PrivilegeInfo) obj;
		return classMode.equals(other.classMode) //
				&& groupId.equals(other.getGroupId()) //
				&& getPrivilegedObjectId().equals(other.getPrivilegedObjectId());
	}

	public static class Builder implements org.cmdbuild.common.Builder<PrivilegeInfo> {

		private Long groupId;
		private SerializablePrivilege privilegedObject;
		private PrivilegeMode classMode;
		private String privilegeFilter;
		private String[] attributesPrivileges = null;
		private CardEditMode cardEditMode;

		public Builder withGroupId(final Long groupId) {
			this.groupId = groupId;
			return this;
		}

		public Builder withPrivilegedObject(final SerializablePrivilege privilegedObject) {
			this.privilegedObject = privilegedObject;
			return this;
		}

		public Builder withPrivilegeMode(final PrivilegeMode classMode) {
			this.classMode = classMode;
			return this;
		}

		public Builder withCardEditMode(final CardEditMode cardEditMode) {
			this.cardEditMode = cardEditMode;
			return this;
		}

		public Builder withAttributesPrivileges(final String[] attributesPrivileges) {
			this.attributesPrivileges = attributesPrivileges;
			return this;
		}

		public Builder withPrivilegeFilter(final String privilegeFilter) {
			this.privilegeFilter = privilegeFilter;
			return this;
		}

		@Override
		public PrivilegeInfo build() {
			return new PrivilegeInfo(this);
		}

	}

}