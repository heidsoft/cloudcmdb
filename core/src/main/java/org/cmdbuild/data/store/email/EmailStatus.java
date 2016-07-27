package org.cmdbuild.data.store.email;

public enum EmailStatus {
	RECEIVED("Received"), //
	DRAFT("Draft"), //
	OUTGOING("Outgoing"), //
	SENT("Sent"), //
	;

	public static final String LOOKUP_TYPE = "EmailStatus";

	private String lookupName;

	private EmailStatus(final String lookupName) {
		this.lookupName = lookupName;
	}

	public String getLookupName() {
		return lookupName;
	}

	public static EmailStatus of(final String lookupName) {
		for (final EmailStatus status : EmailStatus.values()) {
			if (status.getLookupName().equals(lookupName)) {
				return status;
			}
		}
		throw new IllegalArgumentException();
	}

}