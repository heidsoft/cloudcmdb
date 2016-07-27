package org.cmdbuild.auth.user;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.auth.PasswordAuthenticator.PasswordChanger;

public class AuthenticatedUserImpl extends ForwardingUser implements AuthenticatedUser {

	public static final AuthenticatedUser ANONYMOUS_USER = new AnonymousUser();

	public static AuthenticatedUser newInstance(final CMUser user) {
		return (user == null) ? ANONYMOUS_USER : new AuthenticatedUserImpl(user);
	}

	private final CMUser user;
	private PasswordChanger passwordChanger;

	protected AuthenticatedUserImpl(final CMUser user) {
		Validate.notNull(user);
		this.user = user;
	}

	@Override
	protected CMUser delegate() {
		return user;
	}

	@Override
	public boolean isAnonymous() {
		return false;
	}

	/*
	 * Password change
	 */

	@Override
	public void setPasswordChanger(final PasswordChanger passwordChanger) {
		this.passwordChanger = passwordChanger;
	}

	@Override
	public final boolean changePassword(final String oldPassword, final String newPassword) {
		return canChangePassword() && passwordChanger.changePassword(oldPassword, newPassword);
	}

	@Override
	public final boolean canChangePassword() {
		return passwordChanger != null;
	}

}
