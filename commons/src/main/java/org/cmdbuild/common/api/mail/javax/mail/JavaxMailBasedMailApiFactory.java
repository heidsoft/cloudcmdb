package org.cmdbuild.common.api.mail.javax.mail;

import org.cmdbuild.common.api.mail.Configuration.All;
import org.cmdbuild.common.api.mail.Configuration.Input;
import org.cmdbuild.common.api.mail.Configuration.Output;
import org.cmdbuild.common.api.mail.MailApi;
import org.cmdbuild.common.api.mail.MailApiFactory;

/**
 * Default Mail API factory.
 */
public class JavaxMailBasedMailApiFactory implements MailApiFactory {

	@Override
	public MailApi create(final Input input, final Output output) {
		return new JavaxMailBasedMailApi(input, output);
	}

	@Override
	public MailApi create(final All all) {
		return create(all, all);
	}

}
