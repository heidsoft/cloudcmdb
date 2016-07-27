package org.cmdbuild.data.store.email;

import org.cmdbuild.data.store.Storable;

public interface EmailAccount extends Storable {

	Long getId();

	String getName();

	boolean isDefault();

	String getUsername();

	String getPassword();

	String getAddress();

	String getSmtpServer();

	Integer getSmtpPort();

	boolean isSmtpSsl();

	boolean isSmtpStartTls();

	boolean isSmtpConfigured();

	String getOutputFolder();

	String getImapServer();

	Integer getImapPort();

	boolean isImapSsl();

	boolean isImapStartTls();

	boolean isImapConfigured();

}