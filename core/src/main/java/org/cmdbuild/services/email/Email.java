package org.cmdbuild.services.email;

import org.joda.time.DateTime;

public interface Email {

	DateTime getDate();

	String getFromAddress();

	Iterable<String> getToAddresses();

	Iterable<String> getCcAddresses();

	Iterable<String> getBccAddresses();

	String getSubject();

	String getContent();

	Iterable<Attachment> getAttachments();

	String getAccount();

	long getDelay();

}
