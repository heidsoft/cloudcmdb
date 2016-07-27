package org.cmdbuild.common.api.mail;

import org.cmdbuild.common.api.mail.Configuration.All;
import org.cmdbuild.common.api.mail.Configuration.Input;
import org.cmdbuild.common.api.mail.Configuration.Output;

/**
 * {@link MailApi} factory.
 */
public interface MailApiFactory {

	/**
	 * Creates a new instance of {@link MailApi} based on specified
	 * configurations.
	 * 
	 * @param input
	 * @param output
	 * 
	 * @return a new {@link MailApi} instance.
	 */
	public abstract MailApi create(Input input, Output output);

	/**
	 * Creates a new instance of {@link MailApi} based on specified
	 * configuration.
	 * 
	 * @param all
	 * 
	 * @return a new {@link MailApi} instance.
	 */
	public abstract MailApi create(All all);

}
