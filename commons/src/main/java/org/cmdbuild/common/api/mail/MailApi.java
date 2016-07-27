package org.cmdbuild.common.api.mail;

/**
 * Mail API class.
 */
public interface MailApi {

	/**
	 * Creates a new mail.
	 * 
	 * @return a new mail object.
	 */
	SendableNewMail newMail();

	/**
	 * Creates a new mail queue.
	 * 
	 * @return a new mail queue object.
	 */
	NewMailQueue newMailQueue();

	/**
	 * Selects a folder.
	 * 
	 * @param folder
	 *            is name of the folder.
	 * 
	 * @return a new {@link SelectFolder} object.
	 */
	SelectFolder selectFolder(String folder);

	/**
	 * Selects a fetched mail.
	 * 
	 * @param mail
	 *            is the fetched mail.
	 * 
	 * @return a new {@link SelectMail} object.
	 */
	SelectMail selectMail(FetchedMail mail);

}
