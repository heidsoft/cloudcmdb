package org.cmdbuild.data.store.email;

import org.cmdbuild.data.store.Storable;

public interface EmailTemplate extends Storable {

	Long getId();

	String getName();

	String getDescription();

	String getFrom();

	String getTo();

	String getCc();

	String getBcc();

	String getSubject();

	String getBody();

	Long getAccount();

	boolean isKeepSynchronization();

	boolean isPromptSynchronization();

	long getDelay();

}