package org.cmdbuild.data.store.email;

import java.util.Map;

import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.dao.BaseStorableConverter;

import com.google.common.collect.Maps;

public class EmailAccountStorableConverter extends BaseStorableConverter<EmailAccount> {

	private static final String EMAIL_ACCOUNT = "_EmailAccount";

	private static final String IS_DEFAULT = "IsDefault";
	private static final String CODE = Constants.CODE_ATTRIBUTE;
	private static final String ADDRESS = "Address";
	private static final String USERNAME = "Username";
	private static final String PASSWORD = "Password";
	private static final String SMTP_SERVER = "SmtpServer";
	private static final String SMTP_PORT = "SmtpPort";
	private static final String SMTP_SSL = "SmtpSsl";
	private static final String SMTP_STARTTLS = "SmtpStartTls";
	private static final String OUTPUT_FOLDER = "OutputFolder";
	private static final String IMAP_SERVER = "ImapServer";
	private static final String IMAP_PORT = "ImapPort";
	private static final String IMAP_SSL = "ImapSsl";
	private static final String IMAP_STARTTLS = "ImapStartTls";

	@Override
	public String getClassName() {
		return EMAIL_ACCOUNT;
	}

	@Override
	public EmailAccount convert(final CMCard card) {
		return DefaultEmailAccount.newInstance() //
				.withId(card.getId()) //
				.withDefaultStatus(defaultBoolean(card.get(IS_DEFAULT, Boolean.class), false)) //
				.withName(card.get(CODE, String.class)) //
				.withAddress(card.get(ADDRESS, String.class)) //
				.withUsername(card.get(USERNAME, String.class)) //
				.withPassword(card.get(PASSWORD, String.class)) //
				.withSmtpServer(card.get(SMTP_SERVER, String.class)) //
				.withSmtpPort(card.get(SMTP_PORT, Integer.class)) //
				.withSmtpSsl(defaultBoolean(card.get(SMTP_SSL, Boolean.class), false)) //
				.withSmtpStartTls(defaultBoolean(card.get(SMTP_STARTTLS, Boolean.class), false)) //
				.withOutputFolder(card.get(OUTPUT_FOLDER, String.class)) //
				.withImapServer(card.get(IMAP_SERVER, String.class)) //
				.withImapPort(card.get(IMAP_PORT, Integer.class)) //
				.withImapSsl(defaultBoolean(card.get(IMAP_SSL, Boolean.class), false)) //
				.withImapStartTls(defaultBoolean(card.get(IMAP_STARTTLS, Boolean.class), false)) //
				.build();
	}

	@Override
	public String getIdentifierAttributeName() {
		return CODE;
	}

	private boolean defaultBoolean(final Boolean value, final boolean defaultValue) {
		return (value == null) ? defaultValue : value;
	}

	@Override
	public Map<String, Object> getValues(final EmailAccount storable) {
		final Map<String, Object> values = Maps.newHashMap();
		values.put(IS_DEFAULT, storable.isDefault());
		values.put(CODE, storable.getName());
		values.put(ADDRESS, storable.getAddress());
		values.put(USERNAME, storable.getUsername());
		values.put(PASSWORD, storable.getPassword());
		values.put(SMTP_SERVER, storable.getSmtpServer());
		values.put(SMTP_PORT, storable.getSmtpPort());
		values.put(SMTP_SSL, storable.isSmtpSsl());
		values.put(SMTP_STARTTLS, storable.isSmtpStartTls());
		values.put(OUTPUT_FOLDER, storable.getOutputFolder());
		values.put(IMAP_SERVER, storable.getImapServer());
		values.put(IMAP_PORT, storable.getImapPort());
		values.put(IMAP_SSL, storable.isImapSsl());
		values.put(IMAP_STARTTLS, storable.isImapStartTls());
		return values;
	}

}
