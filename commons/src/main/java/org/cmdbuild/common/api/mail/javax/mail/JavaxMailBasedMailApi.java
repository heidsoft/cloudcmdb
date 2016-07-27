package org.cmdbuild.common.api.mail.javax.mail;

import java.security.Security;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;

import org.cmdbuild.common.api.mail.Configuration.Input;
import org.cmdbuild.common.api.mail.Configuration.Output;
import org.cmdbuild.common.api.mail.FetchedMail;
import org.cmdbuild.common.api.mail.MailApi;
import org.cmdbuild.common.api.mail.NewMailQueue;
import org.cmdbuild.common.api.mail.SelectFolder;
import org.cmdbuild.common.api.mail.SelectMail;
import org.cmdbuild.common.api.mail.SendableNewMail;

public class JavaxMailBasedMailApi implements MailApi {

	private final Input input;
	private final Output output;

	@SuppressWarnings("restriction")
	JavaxMailBasedMailApi(final Input input, final Output output) {
		this.input = input;
		this.output = output;

		final MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
		CommandMap.setDefaultCommandMap(mc);

		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
	}

	@Override
	public SendableNewMail newMail() {
		return new SendableNewMailImpl(input, output);
	}

	@Override
	public NewMailQueue newMailQueue() {
		return new NewMailQueueImpl(input, output);
	}

	@Override
	public SelectFolder selectFolder(final String folder) {
		return new SelectFolderImpl(input, folder);
	}

	@Override
	public SelectMail selectMail(final FetchedMail mail) {
		return new SelectMailImpl(input, mail);
	}

}
