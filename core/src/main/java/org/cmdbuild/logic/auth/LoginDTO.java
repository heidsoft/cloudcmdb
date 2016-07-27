package org.cmdbuild.logic.auth;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class LoginDTO {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<LoginDTO> {

		private String loginString;
		private String unencryptedPassword;
		private String loginGroupName;
		public boolean passwordRequired = true;
		public boolean serviceUsersAllowed;

		@Override
		public LoginDTO build() {
			return new LoginDTO(this);
		}

		/**
		 * 
		 * @param loginString
		 *            could be the either the username or the email
		 * @return
		 */
		public Builder withLoginString(final String loginString) {
			this.loginString = loginString;
			return this;
		}

		public Builder withPassword(final String unencryptedPassword) {
			this.unencryptedPassword = unencryptedPassword;
			this.passwordRequired = true;
			return this;
		}

		public Builder withGroupName(final String loginGroupName) {
			this.loginGroupName = loginGroupName;
			return this;
		}

		public Builder withNoPasswordRequired() {
			this.passwordRequired = false;
			return this;
		}

		public Builder withServiceUsersAllowed(final boolean serviceUsersAllowed) {
			this.serviceUsersAllowed = serviceUsersAllowed;
			return this;
		}

	}

	private final String loginString;
	private final String unencryptedPassword;
	private final String loginGroupName;
	private final boolean passwordRequired;
	private final boolean serviceUsersAllowed;

	private LoginDTO(final Builder builder) {
		this.loginString = builder.loginString;
		this.unencryptedPassword = builder.unencryptedPassword;
		this.loginGroupName = builder.loginGroupName;
		this.passwordRequired = builder.passwordRequired;
		this.serviceUsersAllowed = builder.serviceUsersAllowed;
	}

	public static Builder newInstance() {
		return new Builder();
	}

	public String getLoginString() {
		return loginString;
	}

	public String getPassword() {
		return unencryptedPassword;
	}

	public String getLoginGroupName() {
		return loginGroupName;
	}

	public boolean isPasswordRequired() {
		return passwordRequired;
	}

	public boolean isServiceUsersAllowed() {
		return serviceUsersAllowed;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof LoginDTO)) {
			return false;
		}
		final LoginDTO other = LoginDTO.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.loginString, other.loginString) //
				.append(this.unencryptedPassword, other.unencryptedPassword) //
				.append(this.loginGroupName, other.loginGroupName) //
				.append(this.passwordRequired, other.passwordRequired) //
				.append(this.serviceUsersAllowed, other.serviceUsersAllowed) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(loginString) //
				.append(unencryptedPassword) //
				.append(loginGroupName) //
				.append(passwordRequired) //
				.append(serviceUsersAllowed) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE).toString();
	}

}
