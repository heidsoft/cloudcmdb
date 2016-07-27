package org.cmdbuild.services.email;

import static java.util.Arrays.asList;
import static org.cmdbuild.system.SystemUtils.isMailDebugEnabled;

import java.util.List;

import org.cmdbuild.common.api.mail.Configuration;
import org.cmdbuild.data.store.email.EmailAccount;
import org.slf4j.Logger;

public class AllConfigurationWrapper implements Configuration.All {

	public static AllConfigurationWrapper of(final EmailAccount account) {
		return new AllConfigurationWrapper(account);
	}

	private final EmailAccount account;

	private AllConfigurationWrapper(final EmailAccount account) {
		this.account = account;
	}

	@Override
	public boolean isDebug() {
		return isMailDebugEnabled();
	}

	@Override
	public Logger getLogger() {
		return EmailService.logger;
	}

	@Override
	public String getOutputProtocol() {
		final boolean useSsl = Boolean.valueOf(account.isSmtpSsl());
		return useSsl ? PROTOCOL_SMTPS : PROTOCOL_SMTP;
	}

	@Override
	public boolean isOutputStartTlsEnabled() {
		return Boolean.valueOf(account.isSmtpStartTls());
	}

	@Override
	public String getOutputHost() {
		return account.getSmtpServer();
	}

	@Override
	public Integer getOutputPort() {
		return account.getSmtpPort();
	}

	@Override
	public String getOutputUsername() {
		return account.getUsername();
	}

	@Override
	public String getOutputPassword() {
		return account.getPassword();
	}

	@Override
	public List<String> getOutputFromRecipients() {
		return asList(account.getAddress());
	}

	@Override
	public String getOutputFolder() {
		return account.getOutputFolder();
	}

	@Override
	public String getInputProtocol() {
		final boolean useSsl = Boolean.valueOf(account.isImapSsl());
		return useSsl ? PROTOCOL_IMAPS : PROTOCOL_IMAP;
	}

	@Override
	public boolean isInputStartTlsEnabled() {
		return Boolean.valueOf(account.isImapStartTls());
	}

	@Override
	public String getInputHost() {
		return account.getImapServer();
	}

	@Override
	public Integer getInputPort() {
		return account.getImapPort();
	}

	@Override
	public String getInputUsername() {
		return account.getUsername();
	}

	@Override
	public String getInputPassword() {
		return account.getPassword();
	}

}