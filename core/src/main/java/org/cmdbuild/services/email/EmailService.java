package org.cmdbuild.services.email;

import org.cmdbuild.logger.Log;
import org.slf4j.Logger;

/**
 * Service for coordinate e-mail operations and persistence.
 */
public interface EmailService {

	Logger logger = Log.EMAIL;

	/**
	 * Folders specifications for {@link Email} reception.
	 */
	interface Folders {

		String incoming();

		String processed();

		String rejected();

		boolean rejectNotMatching();

	}

	/**
	 * Handler for {@link Email} reception.
	 */
	interface EmailCallbackHandler {

		void handle(Email email);

	}

	/**
	 * Sends the specified mail.
	 * 
	 * @param email
	 * 
	 * @throws EmailServiceException
	 *             if there is any problem.
	 */
	void send(Email email) throws EmailServiceException;

	/**
	 * Retrieves mails from mailbox.
	 * 
	 * @param folders
	 *            contains all folders specifications.
	 * @param callback
	 * 
	 * @throws EmailServiceException
	 *             if there is any problem.
	 */
	void receive(Folders folders, EmailCallbackHandler callback) throws EmailServiceException;

	/**
	 * Retrieves mails from mailbox.
	 * 
	 * @param folders
	 *            contains all folders specifications.
	 * 
	 * @throws EmailServiceException
	 *             if there is any problem.
	 */
	Iterable<Email> receive(Folders folders) throws EmailServiceException;

}
