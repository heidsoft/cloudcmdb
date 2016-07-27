package org.cmdbuild.services.email;

import org.cmdbuild.data.store.email.EmailAccount;

import com.google.common.base.Supplier;

/**
 * {@link EmailService} factory class.
 */
public interface EmailServiceFactory {

	/**
	 * Creates a new {@link EmailService}.
	 * 
	 * @return the created {@link EmailService}.
	 */
	EmailService create();

	/**
	 * Creates a new {@link EmailService} with the specific {@link EmailAccount}
	 * .
	 * 
	 * @param emailAccountSupplier
	 * 
	 * @return the created {@link EmailService}.
	 */
	EmailService create(Supplier<EmailAccount> emailAccountSupplier);

}
