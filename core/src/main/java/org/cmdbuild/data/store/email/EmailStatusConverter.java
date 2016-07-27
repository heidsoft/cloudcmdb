package org.cmdbuild.data.store.email;

public interface EmailStatusConverter {

	EmailStatus fromId(Long id);

	Long toId(EmailStatus status);

}
