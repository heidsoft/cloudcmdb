package org.cmdbuild.common.api.mail;

/**
 * Select folder interface.
 */
public interface SelectFolder {

	/**
	 * Fetches all mails within the selected folder.
	 * 
	 * @return all fetched mails.
	 * 
	 * @throws MailException
	 *             if there is any problem.
	 */
	Iterable<FetchedMail> fetch() throws MailException;

}
